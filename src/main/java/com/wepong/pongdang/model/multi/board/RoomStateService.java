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

    public void setRoomState(Long roomId, int maxRound, int pot) {
        RoomStateDTO state = RoomStateDTO.builder()
                .currentTurn(1)
                .round(1)
                .maxRound(maxRound)
                .pot(pot)
                .build();
        roomStateDAO.addState(roomId, state);
    }

    public void removeRoomState(Long roomId) {
        roomStateDAO.removeState(roomId);
    }
}
