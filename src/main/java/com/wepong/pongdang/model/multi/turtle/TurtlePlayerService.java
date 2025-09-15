package com.wepong.pongdang.model.multi.turtle;

import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurtlePlayerService {

    private final TurtlePlayerDAO turtlePlayerDAO;
    private final AuthService authService;

    public TurtlePlayerDTO getPlayer(Long roomId, Long userId) {
        return turtlePlayerDAO.getPlayer(roomId, userId);
    }

    // 거북이 게임방 플레이어 상세 조회
    public List<TurtlePlayerDTO> getPlayers(Long roomId) {
        List<TurtlePlayerDTO> players = turtlePlayerDAO.getAll(roomId);
        return players;
    }

    public void enterPlayer(Long roomId, Long userId) {
        UserEntity user = authService.findById(userId);

        TurtlePlayerDTO player = TurtlePlayerDTO.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .roomId(roomId)
                .isReady(false)
                .build();

        turtlePlayerDAO.addPlayer(roomId, player);
    }

    public void exitPlayer(Long roomId, Long userId) {
        turtlePlayerDAO.removePlayer(roomId, userId);
    }

    public boolean exists(Long roomId, Long userId) {
        List<TurtlePlayerDTO> players = getPlayers(roomId);
        if (players == null) return false;
        return players.stream().anyMatch(p -> p.getUserId().equals(userId));
    }

    public void setRandomTurtle(Long userId, Long roomId, int turtleCount) {
        TurtlePlayerDTO player = getPlayer(roomId, userId);

        if (player != null && "random".equals(player.getTurtleId())) {
            List<String> allColors;
            switch (turtleCount) {
                case 4:
                    allColors = List.of("green", "orange", "pink", "yellow");
                    break;
                case 6:
                    allColors = List.of("green", "orange", "pink", "yellow", "brown", "purple");
                    break;
                case 8:
                default:
                    allColors = List.of("green", "orange", "pink", "yellow", "brown", "purple", "gray", "blue");
                    break;
            }

            List<TurtlePlayerDTO> players = getPlayers(roomId);
            Set<String> usedColors = players.stream()
                    .map(TurtlePlayerDTO::getTurtleId)
                    .filter(id -> id != null && !"random".equals(id))
                    .collect(Collectors.toSet());

            List<String> availableColors = allColors.stream()
                    .filter(color -> !usedColors.contains(color))
                    .collect(Collectors.toList());

            if (!availableColors.isEmpty()) {
                String randomColor = availableColors.get(new Random().nextInt(availableColors.size()));
                player.setTurtleId(randomColor);
            }
        }
    }
}
