package com.wepong.pongdang.model.multi.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomStateService {

    private final RoomStateDAO roomStateDAO;

    public RoomStateDTO getState(Long roomId) {
        return roomStateDAO.getState(roomId);
    }

    public void setRoomState(Long roomId) {
        RoomStateDTO state = RoomStateDTO.builder()
                .currentTurn(0)
                .round(1)
                .maxRound(10)
                .pot(0)
                .doubleCount(0)
                .isDouble(false)
                .build();
        roomStateDAO.addState(roomId, state);
    }

    public void removeRoomState(Long roomId) {
        roomStateDAO.removeState(roomId);
    }
}
