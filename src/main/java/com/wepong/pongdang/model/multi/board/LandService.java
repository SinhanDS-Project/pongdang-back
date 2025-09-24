package com.wepong.pongdang.model.multi.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LandService {

    private final LandDAO landDAO;

    public LandDTO getLand(Long roomId, int landId) {
        return landDAO.getLand(roomId, landId);
    }

    public List<LandDTO> getLands(Long roomId) {
        return landDAO.getLands(roomId);
    }

    public void setLands(Long roomId) {
        List<LandDTO> lands = new ArrayList<>();
        lands.add(new LandDTO(0, "출발", 15, 0, null, null));
        lands.add(new LandDTO(1, "케이뱅크", 3, 6, null, null));
        lands.add(new LandDTO(2, "토스뱅크", 3, 6, null, null));
        lands.add(new LandDTO(3, "퀴즈", 5, 5, null, null));
        lands.add(new LandDTO(4, "수협은행", 4, 8, null, null));
        lands.add(new LandDTO(5, "카카오뱅크", 4, 8, null, null));
        lands.add(new LandDTO(6, "휴게소", 0, 0, null, null));
        lands.add(new LandDTO(7, "한국씨티은행", 5, 10, null, null));
        lands.add(new LandDTO(8, "HSBC", 5, 10, null, null));
        lands.add(new LandDTO(9, "웰스파고", 6, 12, null, null));
        lands.add(new LandDTO(10, "퀴즈", 5, 5, null, null));
        lands.add(new LandDTO(11, "IM뱅크", 6, 12, null, null));
        lands.add(new LandDTO(12, "금고", 0, 0, null, null));
        lands.add(new LandDTO(13, "SC제일은행", 7, 14, null, null));
        lands.add(new LandDTO(14, "KDB산업은행", 7, 14, null, null));
        lands.add(new LandDTO(15, "저금", 0, 5, null, null));
        lands.add(new LandDTO(16, "NH농협은행", 8, 16, null, null));
        lands.add(new LandDTO(17,  "IBK기업은행", 8, 16, null, null));
        lands.add(new LandDTO(18, "보너스", 0, 0, null, null));
        lands.add(new LandDTO(19, "우리은행", 9, 18, null, null));
        lands.add(new LandDTO(20, "하나은행", 9, 18, null, null));
        lands.add(new LandDTO(21, "KB국민은행", 9, 18, null, null));
        lands.add(new LandDTO(22, "세금", 0, 10, null, null));
        lands.add(new LandDTO(23, "신한은행", 10, 20, null, null));

        landDAO.addLands(roomId, lands);
    }

    public void removeLands(Long roomId) {
        landDAO.removeLands(roomId);
    }
}
