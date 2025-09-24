package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.response.HistoryResponseDTO;
import com.wepong.pongdang.entity.GameHistoryEntity;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.repository.GameHistoryRepository;
import com.wepong.pongdang.repository.PongHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoryService {

    private final GameHistoryRepository gameHistoryRepository;
    private final PongHistoryRepository pongHistoryRepository;
    
    public HistoryResponseDTO.GameResponseDTO gameHistoryList(Long userId, int page) {
        int size = 10;
        int offset = (page - 1) * size;
        Pageable pageable = PageRequest.of(offset / size, size);
        Page<GameHistoryEntity> list = gameHistoryRepository.findByUserId(userId, pageable);

        Page<HistoryResponseDTO.GameDetailResponseDTO> details = list.map(HistoryResponseDTO.GameDetailResponseDTO::from);

        return HistoryResponseDTO.GameResponseDTO.from(details);
    }

    public HistoryResponseDTO.PointResponseDTO pointHistoryList(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        Pageable pageable = PageRequest.of(offset / size, size, Sort.Direction.DESC, "createdAt");
        Page<PongHistoryEntity> list = pongHistoryRepository.findByUserId(userId, pageable);

        Page<HistoryResponseDTO.PointDetailResponseDTO> details = list.map(HistoryResponseDTO.PointDetailResponseDTO::from);

        return HistoryResponseDTO.PointResponseDTO.from(details);
    }

    public void insertGameHistory(GameHistoryEntity gameHistoryEntity, UserEntity user) {
        GameHistoryEntity history = GameHistoryEntity.builder()
                .user(user)
                .game(gameHistoryEntity.getGame())
                .pongValue(gameHistoryEntity.getPongValue())
                .rank(gameHistoryEntity.getRank())
                .entryFee(gameHistoryEntity.getEntryFee())
                .build();

        gameHistoryRepository.save(history);
    }

    public void insertPointHistory(PongHistoryEntity pongHistoryEntity, UserEntity user) {
        PongHistoryEntity history = PongHistoryEntity.builder()
                .user(user)
                .type(pongHistoryEntity.getType())
                .amount(pongHistoryEntity.getAmount())
                .build();

        pongHistoryRepository.save(history);
    }
}
