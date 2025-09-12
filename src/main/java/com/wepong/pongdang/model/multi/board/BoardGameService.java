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

    private final Map<Long, List<BoardPlayerDTO>> gameStartPlayersMap = new ConcurrentHashMap<>();
    private final WebSocketService webSocketService;

    // 게임 시작
    public void startGame(Long roomId, String gameType) {
        List<BoardPlayerDTO> startPlayers = boardPlayerService.getPlayers(roomId);

        if(startPlayers != null) {
            gameStartPlayersMap.put(roomId, new ArrayList<>(startPlayers));
        }

        roomStateService.setRoomState(roomId, 10, 0);
        landService.setLands(roomId);

        Map<String, Object> data = new HashMap<>();
        data.put("roomState", roomStateService.getState(roomId));
        data.put("lands", landService.getLands(roomId));

        webSocketService.sendGame(roomId, "game_start", gameType, data);
    }

    public void processUserLose(Long roomId, Long userId) {
        // 나간사람  패배처리
        List<BoardPlayerDTO> startPlayers = gameStartPlayersMap.get(roomId);
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

    public void toll(Long roomId, Long userId, int landId) {
        LandDTO land = landService.getLand(roomId, landId);
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);
        BoardPlayerDTO owner = boardPlayerService.getPlayer(roomId, land.getOwnerId());

        // 통행료 지불 및 지급
        player.setBalance(player.getBalance() - land.getToll());
        owner.setBalance(owner.getBalance() + land.getToll());

        // 파산 처리
        bankruptcy(player, land);
    }

    public void roll(Long roomId, Long userId, int dice) {
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);

        int position = (player.getPosition() + dice) % 24;
        player.setPosition(position);
    }

    public void purchase(Long roomId, Long userId, int landId) {
        LandDTO land = landService.getLand(roomId, landId);
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);

        player.setBalance(land.getPrice());
        land.setOwnerId(player.getUserId());
        land.setColor(player.getTurtleId());
    }

    public void bank(Long roomId, Long userId, int landId) {
        LandDTO land = landService.getLand(roomId, landId);
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);

        // 저금/세금 지불
        player.setBalance(player.getBalance() - land.getToll());

        // 파산 처리
        bankruptcy(player, land);
    }

    private void bankruptcy(BoardPlayerDTO player, LandDTO land) {
        if(player.getBalance() < land.getToll()) {
            player.setActive(false);
        }
    }

    public void salary(Long roomId, Long userId, int landId) {
        LandDTO land = landService.getLand(roomId, landId);
        BoardPlayerDTO player = boardPlayerService.getPlayer(roomId, userId);

        player.setBalance(player.getBalance() + land.getToll());
    }
}
