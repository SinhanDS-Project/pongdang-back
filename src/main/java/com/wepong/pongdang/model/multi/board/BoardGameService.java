package com.wepong.pongdang.model.multi.board;

import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.dto.response.QuizResponseDTO;
import com.wepong.pongdang.entity.*;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.RankType;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.exception.GameNotFoundException;
import com.wepong.pongdang.repository.RewardPerResultRepository;
import com.wepong.pongdang.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BoardGameService {

    private final WalletService walletService;
    private final GameService gameService;
    private final HistoryService historyService;
    private final GameRoomService gameRoomService;
    private final AuthService authService;
    private final BoardPlayerService boardPlayerService;
    private final RoomStateService roomStateService;
    private final LandService landService;
    private final QuizService quizService;

    private final Map<Long, List<BoardPlayerDTO>> startBoardPlayersMap = new ConcurrentHashMap<>();
    private final WebSocketService webSocketService;
    private final RewardPerResultRepository rewardPerResultRepository;

    // 게임 시작
    public void startGame(Long roomId, String gameType) {
        List<BoardPlayerDTO> startPlayers = boardPlayerService.getPlayers(roomId);

        if(startPlayers != null) {
            // 턴 세팅
            for (int i = 0; i < startPlayers.size(); i++) {
                startPlayers.get(i).setTurnOrder(i);
            }

            // 랜덤 거북이 세팅
            boardPlayerService.setRandomTurtle(roomId);

            startBoardPlayersMap.put(roomId, new ArrayList<>(startPlayers));
        }

        roomStateService.setRoomState(roomId);
        landService.setLands(roomId);

        Map<String, Object> data = new HashMap<>();
        data.put("roomState", roomStateService.getState(roomId));
        data.put("lands", landService.getLands(roomId));
        data.put("players", startPlayers);

        webSocketService.sendGame(roomId, "game_start", gameType, data);
    }

    public void processUserLose(Long roomId, Long userId) {
        // 나간사람  패배처리
        List<BoardPlayerDTO> startPlayers = startBoardPlayersMap.get(roomId);
        GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
        if (startPlayers != null) {
            for (BoardPlayerDTO player : startPlayers) {
                if (player.getUserId().equals(userId)) {
                    int entryFee = gameroom.getEntryFee();
                    String gameName = gameroom.getGameName();
                    RankType gameResult = RankType.LOSE;
                    int winAmount = 0;
                    // 1) 유저 정보 조회
                    UserEntity userEntity = authService.findById(userId);
                    if (userEntity != null) {
                        walletService.lose(entryFee, userEntity, WalletType.PONG);
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

                        historyService.insertGameHistory(gameHistoryEntity, userEntity);

                        // 포인트 히스토리 저장
                        PongHistoryEntity pongHistoryEntity = PongHistoryEntity.builder()
                                .type(PongHistoryType.GAME_P)
                                .amount(Math.abs(winAmount - entryFee))
                                .build();

                        historyService.insertPointHistory(pongHistoryEntity, userEntity);
                    }
                    break;
                }
            }
            // 2. startPlayers에서 userId 제거
            startPlayers.removeIf(p -> p.getUserId().equals(userId));
        }
    }

    // 주사위 굴리기
    public void roll(Long roomId, Long userId, int dice, boolean isDouble, String gameType) {
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);
        RoomStateDTO roomState = roomStateService.getState(roomId);

        if(player.isSkipTurn() && !isDouble) {
            player.setSkipTurn(false);

            Map<String, Object> msg = new HashMap<>();
            msg.put("message", player.getNickname()+"님 탈출 실패!\uD83C\uDFDD\uFE0F");

            webSocketService.sendGame(roomId, "prison", gameType, msg);

            endTurn(roomId, gameType);
            return;
        }

        String result = String.valueOf(dice);
        if(isDouble) {
            roomState.setDoubleCount(roomState.getDoubleCount() + 1);
            result = dice+"(더블)";

            if(roomState.getDoubleCount() >= 3) {
                player.setSkipTurn(true);
                player.setPosition(6);

                roomState.setDouble(false);
                roomState.setDoubleCount(0);

                Map<String, Object> msg = new HashMap<>();
                msg.put("players", boardPlayerService.getPlayers(roomId));
                msg.put("message", player.getNickname()+"님이 무인도에 갇혔습니다!\uD83C\uDFDD\uFE0F");

                webSocketService.sendGame(roomId, "prison", gameType, msg);

                endTurn(roomId, gameType);
                return;
            }
        } else {
            roomState.setDoubleCount(0);
        }

        int startPos = player.getPosition();
        int position = (player.getPosition() + dice) % 24;
        player.setPosition(position);

        roomState.setDouble(isDouble);

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("roomState", roomState);
        data.put("message", player.getNickname()+"님이 "+result+"칸 이동!\uD83C\uDFB2");

        webSocketService.sendGame(roomId, "roll", gameType, data);

        if(player.isSkipTurn()) {
            player.setSkipTurn(false);
            endTurn(roomId, gameType);
        }

        if(position < startPos) {
            player.setBalance(player.getBalance() + 15);

            Map<String, Object> msg = new HashMap<>();
            msg.put("players", boardPlayerService.getPlayers(roomId));
            msg.put("message", player.getNickname()+"님이 월급 획득!\uD83D\uDCB5");

            webSocketService.sendGame(roomId, "salary", gameType, msg);
        }

        if (position == 6) {
            player.setSkipTurn(true);

            // 더블 여부 무시하고 턴 종료
            roomState.setDouble(false);
            roomState.setDoubleCount(0);

            Map<String, Object> msg = new HashMap<>();
            msg.put("players", boardPlayerService.getPlayers(roomId));
            msg.put("message", player.getNickname()+"님이 무인도에 갇혔습니다!\uD83C\uDFDD\uFE0F");

            webSocketService.sendGame(roomId, "prison", gameType, msg);

            endTurn(roomId, gameType);
        }
    }

    // 통행료 지급
    public void toll(Long roomId, Long userId, int landId, String gameType) {
        LandDTO land = landService.getLand(roomId, landId);
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);
        BoardPlayerDTO owner = boardPlayerService.getPlayer(roomId, land.getOwnerId());

        // 통행료 지불 및 지급
        player.setBalance(player.getBalance() - land.getToll());
        owner.setBalance(owner.getBalance() + land.getToll());

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("lands", landService.getLands(roomId));
        data.put("message", player.getNickname()+"님이 "+owner.getNickname()+"님 에게 "+land.getToll()+"G 지불 완료!\uD83D\uDCB0");

        webSocketService.sendGame(roomId, "toll", gameType, data);

        // 파산 처리
        bankruptcy(roomId, player, gameType);

        endTurn(roomId, gameType);
    }

    // 땅 구매
    public void purchase(Long roomId, Long userId, int landId, String gameType) {
        LandDTO land = landService.getLand(roomId, landId);
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);

        player.setBalance(player.getBalance() - land.getPrice());
        land.setOwnerId(player.getUserId());
        land.setColor(player.getTurtleId());

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("lands", landService.getLand(roomId, landId));
        data.put("message", player.getNickname()+"님이 "+land.getName()+" 구매!\uD83D\uDC5B");

        webSocketService.sendGame(roomId, "purchase", gameType, data);

        endTurn(roomId, gameType);
    }

    // 저금/세금 지불
    public void bank(Long roomId, Long userId, int landId, String gameType) {
        LandDTO land = landService.getLand(roomId, landId);
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);

        player.setBalance(player.getBalance() - land.getToll());

        if(land.getLandId() == 15) { // 금고 적립
            RoomStateDTO roomState = roomStateService.getState(roomId);
            roomState.setPot(roomState.getPot() + land.getToll());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("roomState", roomStateService.getState(roomId));
        data.put("lands", landService.getLands(roomId));
        data.put("message", player.getNickname()+"님이 "+land.getToll()+"G 지불 완료!\uD83D\uDCB0");

        webSocketService.sendGame(roomId, "tax", gameType, data);

        // 파산 처리
        bankruptcy(roomId, player, gameType);

        endTurn(roomId, gameType);
    }

    private void bankruptcy(Long roomId, BoardPlayerDTO player, String gameType) {
        RoomStateDTO roomState = roomStateService.getState(roomId);

        if(player.getBalance() < 0) {
            player.setActive(false);

            roomState.setDouble(false);
            roomState.setDoubleCount(0);

            List<LandDTO> lands = landService.getLands(roomId);
            for(LandDTO land : lands) {
                if(player.getUserId().equals(land.getOwnerId())) {
                    land.setOwnerId(null);
                }
            }

            Map<String, Object> msg = new HashMap<>();
            msg.put("players", boardPlayerService.getPlayers(roomId));
            msg.put("lands", landService.getLands(roomId));
            msg.put("roomState", roomStateService.getState(roomId));
            msg.put("message", player.getNickname()+"님 파산!\uD83D\uDCA5");

            webSocketService.sendGame(roomId, "bankruptcy", gameType, msg);

            // 파산 인원 확인
            List<BoardPlayerDTO> players = boardPlayerService.getPlayers(roomId);
            long active = players.stream().filter(BoardPlayerDTO::isActive).count();
            if(active <= 1) {
                endGame(roomId, gameType);
            }
        }
    }

    // 월급/금고
    public void salary(Long roomId, Long userId, int landId, String gameType) {
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);

        RoomStateDTO roomState = roomStateService.getState(roomId);
        int amount = roomState.getPot();
        player.setBalance(player.getBalance() + amount);
        roomState.setPot(0);

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("roomState", roomStateService.getState(roomId));
        data.put("message", player.getNickname()+"님이 "+amount+"G 획득!\uD83D\uDCB0");

        webSocketService.sendGame(roomId, "salary", gameType, data);

        endTurn(roomId, gameType);
    }

    public void endTurn(Long roomId, String gameType) {
        RoomStateDTO roomState = roomStateService.getState(roomId);
        List<BoardPlayerDTO> players = boardPlayerService.getPlayers(roomId);

        if(roomState.isDouble()) {
            return;
        }

        int startTurn = roomState.getCurrentTurn();
        int nextTurn = (startTurn + 1) % players.size();

        while(!players.get(nextTurn).isActive()) {
            nextTurn = (nextTurn + 1) % players.size();
        }

        // 라운드 증가 체크
        if(nextTurn <= startTurn) { // 한 바퀴 돌았으면
            roomState.setRound(roomState.getRound() + 1);
            if(roomState.getRound() > roomState.getMaxRound()) {
                endGame(roomId, gameType);
                return;
            }
        }

        roomState.setCurrentTurn(nextTurn);

        Map<String, Object> data = new HashMap<>();
        data.put("roomState", roomState);
        data.put("message", players.get(nextTurn).getNickname()+" 턴!\uD83D\uDC4B");

        webSocketService.sendGame(roomId, "turn_end", gameType, data);
    }

    public void endGame(Long roomId, String gameType) {
        List<BoardPlayerDTO> players = boardPlayerService.getPlayers(roomId);

        for(BoardPlayerDTO player : players) {
            int totalAssets = player.getBalance(); // 현금
            for(LandDTO land : landService.getLands(roomId)) {
                if(player.getUserId().equals(land.getOwnerId())) { // 부동산
                    totalAssets += land.getPrice() / 2;
                }
            }
            player.setBalance(totalAssets);
        }

        // 내림차순 정렬
        players.sort((p1, p2) -> Integer.compare(p2.getBalance(), p1.getBalance()));
        for(int i = 0; i < players.size(); i++) {
            players.get(i).setRank(i+1);
        }

        saveBoardResult(roomId);

        webSocketService.sendGame(roomId, "game_end", gameType, boardPlayerService.getPlayers(roomId));

        // 메모리 정리
        startBoardPlayersMap.remove(roomId);
        landService.removeLands(roomId);
        roomStateService.removeRoomState(roomId);
    }

    // 퀴즈 결과
    public void quiz(Long roomId, Long userId, int selectIdx, boolean isCorrect, String gameType) {
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);
        if(isCorrect) {
            player.setBalance(player.getBalance() + 5);
        } else {
            player.setBalance(player.getBalance() - 5);
        }

        bankruptcy(roomId, player, gameType);

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("selectIdx", selectIdx);
        data.put("isCorrect", isCorrect);
        data.put("lands", landService.getLands(roomId));
        data.put("message", player.getNickname()+"님의 퀴즈 결과는? "+(isCorrect ? "성공!\uD83C\uDF89" : "실패!\uD83D\uDCA9"));

        webSocketService.sendGame(roomId, "quiz_check", gameType, data);

        endTurn(roomId, gameType);
    }

    // 랜덤 퀴즈 전송
    public void sendQuiz(Long roomId, Long userId, String gameType) {
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);
        QuizResponseDTO.QuizView quiz = quizService.getRandomQuiz();

        Map<String, Object> data = new HashMap<>();
        data.put("quiz", quiz);
        data.put("turnOrder", player.getTurnOrder());

        webSocketService.sendGame(roomId, "quiz", gameType, data);
    }

    // 게임 결과 저장
    @Transactional
    public void saveBoardResult(Long roomId) {
        GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
        List<BoardPlayerDTO> players = boardPlayerService.getPlayers(roomId);
        Long gameLevelId = gameroom.getGameLevelId();
        int entryFee = gameroom.getEntryFee();
        String gameName = gameroom.getGameName();

        for (BoardPlayerDTO player : players) {
            int rank = player.getRank();

            RankType rankType;
            switch (rank) {
                case 1 -> rankType = RankType.FIRST;
                case 2 -> rankType = RankType.SECOND;
                default -> rankType = RankType.LOSE;
            }

            RewardPerResultEntity rewardConfig = rewardPerResultRepository.findByGameLevelIdAndRank(gameLevelId, rankType);

            int reward = rewardConfig.getReward();
            int donation = rewardConfig.getDonation();

            player.setReward(reward - donation);

            UserEntity user = authService.findById(player.getUserId());
            WalletEntity pongWallet = walletService.findByIdAndType(user.getId(), WalletType.PONG);
            WalletEntity donaWallet = walletService.findByIdAndType(user.getId(), WalletType.DONA);

            if (pongWallet != null && donaWallet != null) {
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

                historyService.insertGameHistory(gameHistoryEntity, user);

                // 포인트 히스토리 저장
                PongHistoryEntity rewardResult = PongHistoryEntity.builder()
                        .type(PongHistoryType.GAME_P)
                        .amount(reward)
                        .build();

                PongHistoryEntity entryFeeResult = PongHistoryEntity.builder()
                        .type(PongHistoryType.ENTRY)
                        .amount(entryFee)
                        .build();

                historyService.insertPointHistory(rewardResult, user);
                historyService.insertPointHistory(entryFeeResult, user);

                if (donation > 0) {
                    PongHistoryEntity donaHistory = PongHistoryEntity.builder()
                            .type(PongHistoryType.GAME_D)
                            .amount(donation)
                            .build();

                    historyService.insertPointHistory(donaHistory, user);
                }

                if (rankType.equals(RankType.FIRST)) {
                    webSocketService.sendMain("first", player.getNickname() + "님이 " + gameName + "에서 1등을 차지했습니다! \uD83E\uDD47");
                } else if (rankType.equals(RankType.SECOND)) {
                    webSocketService.sendMain("second", player.getNickname() + "님이 " + gameName + "에서 2등을 차지했습니다! \uD83E\uDD48");
                }
            }
        }
    }
}
