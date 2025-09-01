package com.wepong.pongdang.model.multi.turtle;

import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.dto.response.TurtlePlayerDTO;
import com.wepong.pongdang.entity.GameEntity;
import com.wepong.pongdang.entity.GameHistoryEntity;
import com.wepong.pongdang.entity.GameLevelEntity;
import com.wepong.pongdang.entity.GameRoomEntity;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.RewardPerResultEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.WalletEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.RankType;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.repository.GameRoomRepository;
import com.wepong.pongdang.repository.RewardPerResultRepository;

import com.wepong.pongdang.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TurtleGameService {

	private final PlayerDAO playerDAO;

	private final GameRoomService gameRoomService;
	private final AuthService authService;
	private final GameService gameService;
	private final HistoryService historyService;

	private final RewardPerResultRepository rewardPerResultRepository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<Long, TurtleGameState> gameStates = new ConcurrentHashMap<>();

    private final Map<String, ScheduledFuture<?>> broadcastTasks = new ConcurrentHashMap<>();
    private final Map<Long, List<TurtlePlayerDTO>> gameStartPlayersMap = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> gameFinishMap = new ConcurrentHashMap<>();

	private final WalletService walletService;

	private final GameRoomRepository gameRoomRepository;
    private final PlayerService playerService;

    // 콜백 인터페이스(핸들러에서 정의)
    public interface RaceUpdateCallback {
        void onRaceUpdate(Long roomId, double[] positions);
        void onRaceFinish(Long roomId, int winner, List<Map<String, Object>> results);
    }

    // 게임 시작(레이스 시작)
    public void startGame(Long roomId, int turtleCount, RaceUpdateCallback callback) {
        TurtleGameState state = new TurtleGameState(turtleCount);
        gameStates.put(roomId, state);
        List<TurtlePlayerDTO> startPlayers = playerDAO.getAll(roomId);
		// null 방지
		if (startPlayers != null) {
			gameStartPlayersMap.put(roomId, new ArrayList<>(startPlayers));
		}
        scheduler.schedule(() -> runRaceLoop(roomId, state, callback), 0, TimeUnit.SECONDS);
    }

    // 실제 레이스 루프(30ms마다)
    private void runRaceLoop(Long roomId, TurtleGameState state, RaceUpdateCallback callback) {
        int interval = 30;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (state.isFinished()) return;
                state.updateRace();
                callback.onRaceUpdate(roomId, state.getPositions());
                if (state.isFinished()) {
                	List<Map<String, Object>> results = gameResultAndPointCalc(roomId, state);
                    callback.onRaceFinish(roomId, state.getWinner(), results);
                } else {
                    scheduler.schedule(this, interval, TimeUnit.MILLISECONDS);
                }
            }
        };
        scheduler.schedule(task, 0, TimeUnit.MILLISECONDS);
    }
    
    // 결과에 따른 포인트, 승패 계산
    private List<Map<String, Object>> gameResultAndPointCalc(Long roomId, TurtleGameState state) {
        List<TurtlePlayerDTO> players = playerDAO.getAll(roomId);
        GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
        String gameName = gameroom.getGameName();
        int winner = state.getWinner();

		GameRoomEntity gameRoomEntity = gameRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("게임방이 존재하지 않습니다."));
		GameLevelEntity gameLevel = gameRoomEntity.getGameLevel();

		Long gameLevelId = gameRoomEntity.getGameLevel().getId();
		int entryFee = gameLevel.getEntryFee();

        // 결과 리스트 준비
        List<Map<String, Object>> results = new ArrayList<>();

        for (TurtlePlayerDTO player : players) {
            int selectedTurtle = player.getTurtleId() != null ? Integer.parseInt(player.getTurtleId()) - 1 : -1;
            boolean didWin = (selectedTurtle == winner);

			RankType gameResult = didWin ? RankType.WIN : RankType.LOSE;

			RewardPerResultEntity rewardConfig = rewardPerResultRepository.findByGameLevelIdAndRank(gameLevelId, gameResult);

			int reward = didWin ? rewardConfig.getReward() : 0;
			int donation = didWin ? rewardConfig.getDonation() : 0;
			int pongChange = didWin ? reward : -entryFee;

            saveTurtleRunResult(player.getUserId(), entryFee, reward, donation, gameResult, gameName);

            // ✅ 각 플레이어 결과 Map 저장
            Map<String, Object> result = new HashMap<>();
            result.put("user_uid", player.getUserId());
            result.put("selectedTurtle", selectedTurtle);
            result.put("didWin", didWin);
            result.put("winAmount", reward);
            result.put("pointChange", pongChange);
            results.add(result);
        }

        return results;
    }
    
    // DB 저장
    private void saveTurtleRunResult(Long userId, int entryFee, int reward, int donation, RankType gameResult, String gameName) {
    	UserEntity userEntity = authService.findById(userId);
		WalletEntity pongWallet = walletService.findByIdAndType(userId, WalletType.PONG);
		WalletEntity donaWallet = walletService.findByIdAndType(userId, WalletType.DONA);
        if (userEntity != null) {
        	 if (gameResult.equals(RankType.WIN)) {
				 pongWallet.setPongBalance(pongWallet.getPongBalance() - entryFee + reward);
				 donaWallet.setPongBalance(donaWallet.getPongBalance() + donation);
     	    } else {
				 // 이미 차감된 상태면 생략, 아니면 아래 라인 활성화
				 pongWallet.setPongBalance(pongWallet.getPongBalance() - entryFee);
     	    }

            // 히스토리/포인트 히스토리 등도 기록
            Long gameId = gameService.selectByName(gameName)
            		.stream().findFirst()
            		.orElseThrow(() -> new IllegalStateException("'" + gameName + "' 게임을 찾을 수 없습니다."))
            		.getId();

            GameEntity gameEntity = gameService.selectById(gameId);

            // 게임 히스토리 저장
            GameHistoryEntity gameHistoryEntity = GameHistoryEntity.builder()
                    .game(gameEntity)
                    .entryFee(entryFee)
                    .pongValue(reward)
                    .rank(gameResult)
                    .build();

            historyService.insertGameHistory(gameHistoryEntity, userId);

            // 포인트 히스토리 저장
            PongHistoryEntity pongHistory = PongHistoryEntity.builder()
                    .type(PongHistoryType.GAME_P)
                    .amount(Math.abs(reward - entryFee))
                    .build();

            historyService.insertPointHistory(pongHistory, userId);

            if(donation > 0) {
                PongHistoryEntity donaHistory = PongHistoryEntity.builder()
                        .type(PongHistoryType.GAME_D)
                        .amount(donation)
                        .build();

                historyService.insertPointHistory(donaHistory, userId);
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
                    int betAmount = player.getEntryFee();
                    String gameName = gameroom.getGameName();
                    RankType gameResult = RankType.LOSE;
                    int winAmount = 0;
                    // 1) 유저 정보 조회
                    UserEntity userEntity = authService.findById(userId);
                    if (userEntity != null) {
                        walletService.losePong(betAmount, userEntity.getId());
                        Long gameId = gameService.selectByName(gameName).stream().findFirst()
                                .orElseThrow(() -> new IllegalStateException("'" + gameName + "' 게임을 찾을 수 없습니다."))
                                .getId();

                        GameEntity gameEntity = gameService.selectById(gameId);

                        // 게임 히스토리 저장 (gameName도 같이)
                        GameHistoryEntity gameHistoryEntity = GameHistoryEntity.builder()
                                .game(gameEntity)
                                .entryFee(betAmount)
                                .pongValue(Math.abs(winAmount - betAmount))
                                .rank(gameResult)
                                .build();

                        historyService.insertGameHistory(gameHistoryEntity, userId);

                        // 포인트 히스토리 저장
                        PongHistoryEntity pongHistoryEntity = PongHistoryEntity.builder()
                                .type(PongHistoryType.GAME_P)
                                .amount(Math.abs(winAmount - betAmount))
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

    public void onGameStart(Long roomId) {
        // 게임 시작 시점의 참가자 전체 정보 저장
        List<TurtlePlayerDTO> startPlayers = playerService.getPlayers(roomId);
        // null 방지
        if (startPlayers != null) {
            gameStartPlayersMap.put(roomId, new ArrayList<>(startPlayers));
        }

        gameRoomService.sendGame(roomId, "game_start");
    }

    // 방에 위치 정보를 스케쥴러로 보내주는 함수
    public void broadcastRaceUpdate(Long roomId, double[] positions) {
        List<Double> posList = new ArrayList<>();
        for(double p : positions) posList.add(p);
        gameRoomService.sendGame(roomId, "race_update", posList);
    }

    public void broadcastRaceFinish(Long roomId, int winner, List<Map<String, Object>> results) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("winner", winner);
        msg.put("results",  results);
        gameRoomService.sendGame(roomId, "race_finish", msg);

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