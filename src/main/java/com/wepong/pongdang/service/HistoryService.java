package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.response.HistoryResponseDTO;
import com.wepong.pongdang.entity.GameHistoryEntity;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.repository.GameHistoryRepository;
import com.wepong.pongdang.repository.PongHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoryService {

    private final GameHistoryRepository gameHistoryRepository;
    private final PongHistoryRepository pongHistoryRepository;

    private final AuthService authService;

    public HistoryResponseDTO.GameResponseDTO gameHistoryList(Long userId) {
        return gameHistoryRepository.findByUserId(userId);
    }

    public HistoryResponseDTO.PointResponseDTO pointHistoryList(Long userId) {
        return pongHistoryRepository.findByUserId(userId);
    }
    
    public int gameHistoryCount(Long userId) {
      return gameHistoryRepository.countByUserId(userId);
    }

    public int pointHistoryCount(Long userId) {
      return pongHistoryRepository.countByUserId(userId);
    }

    
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
        Pageable pageable = PageRequest.of(offset / size, size);
        Page<PongHistoryEntity> list = pongHistoryRepository.findByUserId(userId, pageable);

        Page<HistoryResponseDTO.PointDetailResponseDTO> details = list.map(HistoryResponseDTO.PointDetailResponseDTO::from);

        return HistoryResponseDTO.PointResponseDTO.from(details);
    }

    public void insertGameHistory(GameHistoryEntity gameHistoryEntity, Long userId) {
        UserEntity userEntity = authService.findById(userId);
        GameHistoryEntity history = GameHistoryEntity.builder()
                .user(userEntity)
                .game(gameHistoryEntity.getGame())
                .pongValue(gameHistoryEntity.getPongValue())
                .rank(gameHistoryEntity.getRank())
                .entryFee(gameHistoryEntity.getEntryFee())
                .build();
        gameHistoryRepository.save(history);
    }

    public void insertPointHistory(PongHistoryEntity pongHistoryEntity, Long userId) {
        UserEntity userEntity = authService.findById(userId);

        PongHistoryEntity history = PongHistoryEntity.builder()
                .user(userEntity)
                .type(pongHistoryEntity.getType())
                .amount(pongHistoryEntity.getAmount())
                .build();

        pongHistoryRepository.save(history);
    }

    public void insertPointHistory(Long userId, int amount) {
        UserEntity userEntity = authService.findById(userId);

        PongHistoryEntity history = PongHistoryEntity.builder()
                .user(userEntity)
                .type(PongHistoryType.CHARGE)
                .amount(amount)
                .build();

        pongHistoryRepository.save(history);
    }
}
