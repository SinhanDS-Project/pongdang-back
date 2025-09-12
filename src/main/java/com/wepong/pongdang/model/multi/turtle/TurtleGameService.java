package com.wepong.pongdang.model.multi.turtle;

import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.entity.GameEntity;
import com.wepong.pongdang.entity.GameHistoryEntity;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.RewardPerResultEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.WalletEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.RankType;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.exception.GameNotFoundException;
import com.wepong.pongdang.repository.RewardPerResultRepository;

import com.wepong.pongdang.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class TurtleGameService {

    private final GameRoomService gameRoomService;
    private final AuthService authService;
    private final GameService gameService;
    private final HistoryService historyService;
    private final WebSocketService webSocketService;

    private final RewardPerResultRepository rewardPerResultRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<Long, TurtleGameState> gameStates = new ConcurrentHashMap<>();

    private final Map<Long, ScheduledFuture<?>> broadcastTasks = new ConcurrentHashMap<>();
    private final Map<Long, List<TurtlePlayerDTO>> gameStartPlayersMap = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> gameFinishMap = new ConcurrentHashMap<>();

    private final WalletService walletService;
    private final TurtlePlayerService turtlePlayerService;

    // 콜백 인터페이스(핸들러에서 정의)
    public interface RaceUpdateCallback {
        void onRaceUpdate(Long roomId, double[] positions);
        void onRaceFinish(Long roomId, int winner, List<Map<String, Object>> results);
    }

    // 게임 시작(레이스 시작)
    public void startGame(Long roomId, int turtleCount, RaceUpdateCallback callback) {
        TurtleGameState state = new TurtleGameState(turtleCount);
        gameStates.put(roomId, state);

        List<TurtlePlayerDTO> startPlayers = turtlePlayerService.getPlayers(roomId);
        // null 방지
        if (startPlayers != null) {
            gameStartPlayersMap.put(roomId, new ArrayList<>(startPlayers));
        }

        runRaceLoop(roomId, state, callback);
    }

    // 실제 레이스 루프(30ms마다)
    private void runRaceLoop(Long roomId, TurtleGameState state, RaceUpdateCallback callback) {
        int interval = 30;
        if (broadcastTasks.containsKey(roomId)) return; // 이미 실행 중이면 무시

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    // 1) 레이스 상태 갱신
                    state.updateRace();
                    callback.onRaceUpdate(roomId, state.getPositions());

                    // 2) 종료 체크
                    if (state.isFinished()) {
                        int[] top3 = state.getTop3TurtleIds();
                        List<Map<String, Object>> results = gameResultAndPointCalc(roomId, top3);

                        callback.onRaceFinish(roomId, state.getWinner(), results);
                        broadcastTasks.remove(roomId);

                        return;
                    }

                    // 3) 아직 종료되지 않았다면 다음 실행 예약
                    ScheduledFuture<?> future = scheduler.schedule(this, interval, TimeUnit.MILLISECONDS);
                    broadcastTasks.put(roomId, future);

                } catch (Exception e) {
                    System.out.println("[ERROR] runRaceLoop exception for room " + roomId + ": " + e.getMessage());
                    e.printStackTrace();
                    broadcastTasks.remove(roomId);
                }
            }
        };

        ScheduledFuture<?> future = scheduler.schedule(task, 0, TimeUnit.MILLISECONDS);
        broadcastTasks.put(roomId, future);
    }

    // 선택 거북이의 순위 판단
    private int rankOf(Integer selectedTurtle, int firstTid, int secondTid, int thirdTid) {
        if (selectedTurtle == null) return 0;
        if (selectedTurtle == firstTid) return 1;
        if (selectedTurtle == secondTid) return 2;
        if (selectedTurtle == thirdTid) return 3;
        return 0;
    }

    // 결과에 따른 포인트, 승패 계산
    @Transactional
    public List<Map<String, Object>> gameResultAndPointCalc(Long roomId, int[] top3) {
        List<TurtlePlayerDTO> players = turtlePlayerService.getPlayers(roomId);

        GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
        String gameName = gameroom.getGameName();
        int entryFee = gameroom.getEntryFee();
        Long gameLevelId = gameroom.getGameLevelId();

        int firstTid = top3.length > 0 ? top3[0] : -1;
        int secondTid = top3.length > 1 ? top3[1] : -1;
        int thirdTid = top3.length > 2 ? top3[2] : -1;

        // 결과 리스트 준비
        List<Map<String, Object>> results = new ArrayList<>();

        for (TurtlePlayerDTO player : players) {
            int selectedTurtle = player.getTurtleId() != null ? Integer.parseInt(player.getTurtleId()) - 1 : -1;
            int rank = rankOf(selectedTurtle, firstTid, secondTid, thirdTid); // 1/2/3 or 0

            RankType rankType;
            switch (rank) {
                case 1 -> rankType = RankType.FIRST;
                case 2 -> rankType = RankType.SECOND;
                case 3 -> rankType = RankType.THIRD;
                default -> rankType = RankType.LOSE;
            }

            RewardPerResultEntity rewardConfig = rewardPerResultRepository.findByGameLevelIdAndRank(gameLevelId, rankType);

            int reward = rewardConfig.getReward();
            int donation = rewardConfig.getDonation();
            int pongChange = reward;

            saveTurtleRunResult(player.getUserId(), entryFee, reward, donation, rankType, gameName);

            // ✅ 각 플레이어 결과 Map 저장
            Map<String, Object> result = new HashMap<>();
            result.put("user_id", player.getUserId());
            result.put("selectedTurtle", selectedTurtle);
            result.put("rank", rank);
            result.put("winAmount", reward);
            result.put("pointChange", pongChange);
            results.add(result);
        }

        return results;
    }

    // DB 저장
    @Transactional
    public void saveTurtleRunResult(Long userId, int entryFee, int reward, int donation, RankType rankType, String gameName) {
        UserEntity userEntity = authService.findById(userId);

        WalletEntity pongWallet = walletService.findByIdAndType(userId, WalletType.PONG);
        WalletEntity donaWallet = walletService.findByIdAndType(userId, WalletType.DONA);

        if (userEntity != null) {
            if (!rankType.equals(RankType.LOSE)) {
                pongWallet.setPongBalance(pongWallet.getPongBalance() - entryFee + reward);
                donaWallet.setPongBalance(donaWallet.getPongBalance() + donation);
            } else {
                pongWallet.setPongBalance(pongWallet.getPongBalance() - entryFee);
            }

            // 히스토리/포인트 히스토리 등도 기록
            Long gameId = gameService.selectByName(gameName)
                    .stream().findFirst()
                    .orElseThrow(() -> new GameNotFoundException())
                    .getId();

            GameEntity gameEntity = gameService.selectById(gameId);

            // 게임 히스토리 저장
            GameHistoryEntity gameHistoryEntity = GameHistoryEntity.builder()
                .game(gameEntity)
                .entryFee(entryFee)
                .pongValue(reward)
                .rank(rankType)
                .build();

            historyService.insertGameHistory(gameHistoryEntity, userId);

            // 포인트 히스토리 저장
            PongHistoryEntity pongHistory = PongHistoryEntity.builder()
                .type(PongHistoryType.GAME_P)
                .amount(Math.abs(reward - entryFee))
                .build();

            historyService.insertPointHistory(pongHistory, userId);

            if (donation > 0) {
                PongHistoryEntity donaHistory = PongHistoryEntity.builder()
                    .type(PongHistoryType.GAME_D)
                    .amount(donation)
                    .build();

                historyService.insertPointHistory(donaHistory, userId);
            }

            if(rankType.equals(RankType.FIRST)) {
                webSocketService.sendMain("first", userEntity.getNickname() + "님이 " + gameName + "에서 1등을 차지했습니다! \uD83E\uDD47");
            } else if(rankType.equals(RankType.SECOND)) {
                webSocketService.sendMain("second", userEntity.getNickname() + "님이 " + gameName + "에서 2등을 차지했습니다! \uD83E\uDD48");
            } else if(rankType.equals(RankType.THIRD)) {
                webSocketService.sendMain("third", userEntity.getNickname() + "님이 " + gameName + "에서 3등을 차지했습니다! \uD83E\uDD49");
            }
        }
    }

    public void processUserLose(Long roomId, Long userId) {
        // 나간사람  패배처리
        List<TurtlePlayerDTO> startPlayers = gameStartPlayersMap.get(roomId);
        GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
        if (startPlayers != null) {
            for (TurtlePlayerDTO player : startPlayers) {
                if (player.getUserId().equals(userId)) {
                    int entryFee = gameroom.getEntryFee();
                    String gameName = gameroom.getGameName();
                    RankType gameResult = RankType.LOSE;
                    int winAmount = 0;
                    // 1) 유저 정보 조회
                    UserEntity userEntity = authService.findById(userId);
                    if (userEntity != null) {
                        walletService.lose(entryFee, userEntity.getId(), WalletType.PONG);
                        Long gameId = gameService.selectByName(gameName).stream().findFirst()
                                .orElseThrow(() -> new GameNotFoundException())
                                .getId();

                        GameEntity gameEntity = gameService.selectById(gameId);

                        // 게임 히스토리 저장 (gameName도 같이)
                        GameHistoryEntity gameHistoryEntity = GameHistoryEntity.builder()
                                .game(gameEntity)
                                .entryFee(entryFee)
                                .pongValue(Math.abs(winAmount - entryFee))
                                .rank(gameResult)
                                .build();

                        historyService.insertGameHistory(gameHistoryEntity, userId);

                        // 포인트 히스토리 저장
                        PongHistoryEntity pongHistoryEntity = PongHistoryEntity.builder()
                                .type(PongHistoryType.GAME_P)
                                .amount(Math.abs(winAmount - entryFee))
                                .build();

                        historyService.insertPointHistory(pongHistoryEntity, userId);
                    }
                    break;
                }
            }
            // 2. startPlayers에서 userId 제거
            startPlayers.removeIf(p -> p.getUserId().equals(userId));
        }
    }

    public void onGameStart(Long roomId, String gameType) {
        // 게임 시작 시점의 참가자 전체 정보 저장
        List<TurtlePlayerDTO> startPlayers = turtlePlayerService.getPlayers(roomId);
        // null 방지
        if (startPlayers != null) {
            gameStartPlayersMap.put(roomId, new ArrayList<>(startPlayers));
        }

        webSocketService.sendGame(roomId, gameType, "game_start");
    }

    // 방에 위치 정보를 스케쥴러로 보내주는 함수
    public void broadcastRaceUpdate(Long roomId, double[] positions, String gameType) {
        List<Double> posList = new ArrayList<>();
        for(double p : positions) posList.add(p);
        webSocketService.sendGame(roomId, "race_update", gameType, posList);
    }

    public void broadcastRaceFinish(Long roomId, int winner, List<Map<String, Object>> results, String gameType) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("winner", winner);
        msg.put("results",  results);
        webSocketService.sendGame(roomId, "race_finish", gameType, msg);

        // 게임 종료 상태
        gameFinishMap.put(roomId, true);
    }

    public void removeGame(Long roomId) {
        gameStartPlayersMap.remove(roomId);
        gameFinishMap.remove(roomId);
        gameRoomService.deleteRoom(roomId);

        ScheduledFuture<?> task = broadcastTasks.remove(roomId);
        if (task != null)
            task.cancel(true);
    }

    public boolean isGameFinished(Long roomId) {
        return Boolean.TRUE.equals(gameFinishMap.get(roomId));
    }
}