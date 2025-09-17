package com.wepong.pongdang.model.multi.board;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BoardPlayerDAO {

    private final Map<Long, List<BoardPlayerDTO>> roomPlayers = new ConcurrentHashMap<>();

    public void addPlayer(Long roomId, BoardPlayerDTO player) {
        roomPlayers.computeIfAbsent(roomId, k -> Collections.synchronizedList(new ArrayList<>())).add(player);
    }

    public void removePlayer(Long roomId, Long userId) {
        List<BoardPlayerDTO> players = roomPlayers.get(roomId);
        if (players != null) {
            synchronized (players) {
                players.removeIf(p -> p.getUserId().equals(userId));
                if(players.isEmpty()) { // 플레이어가 없으면 방 삭제
                    roomPlayers.remove(roomId);
                }
            }
        }
    }

    // 현재 게임방 플레이어 리스트
    public List<BoardPlayerDTO> getAll(Long roomId) {
        return roomPlayers.get(roomId);
    }

    // 현재 로그인한 사용자의 플레이어 정보
    public BoardPlayerDTO getPlayer(Long roomId, Long userId) {
        List<BoardPlayerDTO> players = roomPlayers.get(roomId);
        if (players != null) {
            synchronized (players) {
                for (BoardPlayerDTO player : players) {
                    if (player.getUserId().equals(userId)) {
                        return player;
                    }
                }
            }
        }
        return null;
    }
}
