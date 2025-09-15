package com.wepong.pongdang.model.multi.board;

import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.entity.GameEntity;
import com.wepong.pongdang.entity.GameHistoryEntity;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.RankType;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.exception.GameNotFoundException;
import com.wepong.pongdang.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    private final Map<Long, List<BoardPlayerDTO>> startBoardPlayersMap = new ConcurrentHashMap<>();
    private final WebSocketService webSocketService;

    // 게임 시작
    public void startGame(Long roomId, String gameType) {
        List<BoardPlayerDTO> startPlayers = boardPlayerService.getPlayers(roomId);

        if(startPlayers != null) {
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

    // 주사위 굴리기
    public void roll(Long roomId, Long userId, int dice, boolean isDouble, String gameType) {
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);
        RoomStateDTO roomState = roomStateService.getState(roomId);

        if(player.isSkipTurn() && !isDouble) {
            player.setSkipTurn(false);
            endTurn(roomId, gameType);
            return;
        }

        if(isDouble) {
            roomState.setDouble(true);
            roomState.setDoubleCount(roomState.getDoubleCount() + 1);
            if(roomState.getDoubleCount() >= 3) {
                player.setSkipTurn(true);
                player.setPosition(6);
                roomState.setDouble(false);

                endTurn(roomId, gameType);
                return;
            }
        } else {
            roomState.setDouble(false);
            roomState.setDoubleCount(0);
        }

        int position = (player.getPosition() + dice) % 24;
        player.setPosition(position);

        if (position == 6) {
            player.setSkipTurn(true);
        }

        roomState.setDouble(isDouble);

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("roomState", roomState);

        webSocketService.sendGame(roomId, "roll", gameType, data);
    }

    // 통행료 지급
    public void toll(Long roomId, Long userId, int landId, String gameType) {
        LandDTO land = landService.getLand(roomId, landId);
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);
        BoardPlayerDTO owner = boardPlayerService.getPlayer(roomId, land.getOwnerId());

        // 통행료 지불 및 지급
        player.setBalance(player.getBalance() - land.getToll());
        owner.setBalance(owner.getBalance() + land.getToll());

        // 파산 처리
        bankruptcy(roomId, player);

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("lands", landService.getLands(roomId));

        webSocketService.sendGame(roomId, "toll", gameType, data);

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

        // 파산 처리
        bankruptcy(roomId, player);

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("roomState", roomStateService.getState(roomId));
        data.put("lands", landService.getLands(roomId));

        webSocketService.sendGame(roomId, "tax", gameType, data);

        endTurn(roomId, gameType);
    }

    private void bankruptcy(Long roomId, BoardPlayerDTO player) {
        if(player.getBalance() < 0) {
            player.setActive(false);

            List<LandDTO> lands = landService.getLands(roomId);
            for(LandDTO land : lands) {
                if(player.getUserId().equals(land.getOwnerId())) {
                    land.setOwnerId(null);
                }
            }
        }
    }

    // 월급/금고
    public void salary(Long roomId, Long userId, int landId, String gameType) {
        LandDTO land = landService.getLand(roomId, landId);
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);

        if(land.getLandId() == 0) { // 월급
            player.setBalance(player.getBalance() + land.getToll());
        } else { // 금고 초기화
            RoomStateDTO roomState = roomStateService.getState(roomId);
            player.setBalance(player.getBalance() + roomState.getPot());
            roomState.setPot(0);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("roomState", roomStateService.getState(roomId));

        webSocketService.sendGame(roomId, "salary", gameType, data);

        endTurn(roomId, gameType);
    }

    public void endTurn(Long roomId, String gameType) {
        RoomStateDTO roomState = roomStateService.getState(roomId);
        List<BoardPlayerDTO> players = boardPlayerService.getPlayers(roomId);

        int nextTurn = (roomState.getCurrentTurn() + 1) % players.size();

        while(!players.get(nextTurn).isActive()) {
            nextTurn = (nextTurn + 1) % players.size();
        }

        if(nextTurn == 0) {
            roomState.setRound(roomState.getRound() + 1);

            if(roomState.getRound() > 10) {
                endGame(roomId, gameType);
                return;
            }
        }

        roomState.setCurrentTurn(nextTurn);
        roomState.setDouble(false);

        webSocketService.sendGame(roomId, "turn_end", gameType, roomState);
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

        webSocketService.sendGame(roomId, "game_end", gameType, boardPlayerService.getPlayers(roomId));

        // 메모리 정리
        startBoardPlayersMap.remove(roomId);
        landService.removeLands(roomId);
        roomStateService.removeRoomState(roomId);
    }

    public void quiz(Long roomId, Long userId, int selectIdx, boolean isCorrect, String gameType) {
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);
        if(isCorrect) {
            player.setBalance(player.getBalance() + 5);
        } else {
            player.setBalance(player.getBalance() - 5);
        }

        bankruptcy(roomId, player);

        Map<String, Object> data = new HashMap<>();
        data.put("players", boardPlayerService.getPlayers(roomId));
        data.put("selectIdx", selectIdx);
        data.put("isCorrect", isCorrect);
        data.put("lands", landService.getLands(roomId));

        webSocketService.sendGame(roomId, "quiz_check", gameType, data);

        endTurn(roomId, gameType);
    }
}
