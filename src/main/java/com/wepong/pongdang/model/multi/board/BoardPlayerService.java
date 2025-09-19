package com.wepong.pongdang.model.multi.board;

import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardPlayerService {

    private final BoardPlayerDAO boardPlayerDAO;
    private final AuthService authService;

    public BoardPlayerDTO getPlayer(Long roomId, Long userId) {
        return boardPlayerDAO.getPlayer(roomId, userId);
    }

    // 플레이어 리스트 조회
    public List<BoardPlayerDTO> getPlayers(Long roomId) {
        List<BoardPlayerDTO> players = boardPlayerDAO.getAll(roomId);
        return players;
    }

    public void enterPlayer(Long roomId, Long userId) {
        UserEntity user = authService.findById(userId);

        BoardPlayerDTO player = BoardPlayerDTO.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .roomId(roomId)
                .isReady(false)
                .balance(80)
                .position(0)
                .skipTurn(false)
                .active(true)
                .build();

        boardPlayerDAO.addPlayer(roomId, player);
    }

    public void exitPlayer(Long roomId, Long userId) {
        boardPlayerDAO.removePlayer(roomId, userId);
    }

    public boolean exists(Long roomId, Long userId) {
        List<BoardPlayerDTO> players = getPlayers(roomId);
        if (players == null) return false;
        return players.stream().anyMatch(p -> p.getUserId().equals(userId));
    }

    public void setRandomTurtle(Long userId, Long roomId) {
        BoardPlayerDTO player = getPlayer(roomId, userId);

        if (player != null && "random".equals(player.getTurtleId())) {
            // 1) 전체 색상 목록
            List<String> allColors = List.of("green", "orange", "pink", "yellow");

            // 2) 현재 방의 플레이어들이 이미 선택한 색상 추출
            List<BoardPlayerDTO> players = getPlayers(roomId);
            Set<String> usedColors = players.stream()
                    .map(BoardPlayerDTO::getTurtleId)
                    .filter(id -> id != null && !"random".equals(id))
                    .collect(Collectors.toSet());

            // 3) 선택 가능한 색상만 필터링
            List<String> availableColors = allColors.stream()
                    .filter(color -> !usedColors.contains(color))
                    .collect(Collectors.toList());

            // 4) 무작위 선택
            if (!availableColors.isEmpty()) {
                String randomColor = availableColors.get(new Random().nextInt(availableColors.size()));
                player.setTurtleId(randomColor);
            }
        }
    }
}
