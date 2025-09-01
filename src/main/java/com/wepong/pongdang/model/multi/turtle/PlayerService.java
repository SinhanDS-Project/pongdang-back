package com.wepong.pongdang.model.multi.turtle;

import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.dto.response.TurtlePlayerDTO;
import com.wepong.pongdang.entity.GameRoomEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerDAO playerDAO;
    private final GameRoomService gameRoomService;
    private final AuthService authService;

    public TurtlePlayerDTO getPlayer(Long roomId, Long userId) {
        return playerDAO.getPlayer(roomId, userId);
    }

    // 거북이 게임방 플레이어 상세 조회
    public List<TurtlePlayerDTO> getPlayers(Long roomId) {
        List<TurtlePlayerDTO> players = playerDAO.getAll(roomId);
        return players;
    }

    // 각 게임방 플레이어 수
    public Map<Long, Integer> getAllPlayers() {
        Map<Long, Integer> roomPlayers = new HashMap<>();
        List<Long> roomIds = gameRoomService.selectAll().stream()
                .map(GameRoomEntity::getId)
                .collect(Collectors.toList());
        for (Long roomId : roomIds) {
            List<TurtlePlayerDTO> players = playerDAO.getAll(roomId);
            // 플레이어가 없으면 0 처리
            int count = (players != null) ? players.size() : 0;
            roomPlayers.put(roomId, count);
        }
        return roomPlayers;
    }

    public void enterPlayer(Long roomId, Long userId) {
        GameRoomResponseDTO.GameRoomDetailDTO room = gameRoomService.selectById(roomId);
        UserEntity user = authService.findById(userId);

        TurtlePlayerDTO player = TurtlePlayerDTO.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .roomId(roomId)
                .entryFee(room.getEntryFee())
                .isReady(false)
                .build();

        playerDAO.addPlayer(roomId, player);
    }

    public void exitPlayer(Long roomId, Long userId) {
        playerDAO.removePlayer(roomId, userId);
    }

    public boolean exists(Long roomId, Long userId) {
        List<TurtlePlayerDTO> players = getPlayers(roomId);
        if (players == null) return false;
        return players.stream().anyMatch(p -> p.getUserId().equals(userId));
    }
}
