package com.wepong.pongdang.model.multi.board;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LandDAO {

    private final Map<Long, List<LandDTO>> roomLandsMap = new ConcurrentHashMap<>();

    public void addLands(Long roomId, List<LandDTO> lands) {
        roomLandsMap.put(roomId, Collections.synchronizedList(lands));
    }

    public List<LandDTO> getLands(Long roomId) {
        return roomLandsMap.get(roomId);
    }

    public void removeLands(Long roomId) {
        roomLandsMap.remove(roomId);
    }

    public LandDTO getLand(Long roomId, int landId) {
        List<LandDTO> lands = roomLandsMap.get(roomId);
        if (lands != null) {
            synchronized (lands) {
                for (LandDTO land : lands) {
                    if (land.getLandId().equals(landId)) {
                        return land;
                    }
                }
            }
        }
        return null;
    }
}
