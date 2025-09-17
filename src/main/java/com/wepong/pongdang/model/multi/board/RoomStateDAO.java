package com.wepong.pongdang.model.multi.board;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomStateDAO {

    private final Map<Long, RoomStateDTO> roomStateMap = new ConcurrentHashMap<>();

    public void addState(Long roomId, RoomStateDTO roomState) {
        roomStateMap.put(roomId, roomState);
    }

    public RoomStateDTO getState(Long roomId) {
        return roomStateMap.get(roomId);
    }

    public void removeState(Long roomId) {
        roomStateMap.remove(roomId);
    }
}
