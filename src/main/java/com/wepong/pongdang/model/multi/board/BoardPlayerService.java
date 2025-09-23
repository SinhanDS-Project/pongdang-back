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
                .balance(30)
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

    public void setRandomTurtle(Long roomId) {
        List<BoardPlayerDTO> players = getPlayers(roomId);

        if(players != null && !players.isEmpty()) {
            List<String> allColors = new ArrayList<>(List.of("green", "orange", "pink", "yellow"));

            // 선택 색상 제외
            Set<String> usedColors = players.stream()
                    .map(BoardPlayerDTO::getTurtleId)
                    .filter(id -> id != null && !"random".equals(id))
                    .collect(Collectors.toSet());

            allColors.removeAll(usedColors);

            // 색상 랜덤 섞기
            Collections.shuffle(allColors);

            // 색상 할당
            for (BoardPlayerDTO player : players) {
                if ("random".equals(player.getTurtleId()) && !allColors.isEmpty()) {
                    player.setTurtleId(allColors.remove(0));
                }
            }
        }
    }
}
