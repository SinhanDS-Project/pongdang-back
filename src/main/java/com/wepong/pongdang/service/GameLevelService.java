package com.wepong.pongdang.service;

import com.wepong.pongdang.entity.GameLevelEntity;
import com.wepong.pongdang.repository.GameLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GameLevelService {

	private final GameLevelRepository gameLevelRepository;
	
	public List<GameLevelEntity> selectByGameId(Long gameId){
		return gameLevelRepository.findByGameId(gameId);
	}

	public GameLevelEntity selectByLevelUid(Long levelId) {
		return gameLevelRepository.findById(levelId).orElseThrow(() -> new RuntimeException("레벨이 존재하지 않습니다."));
	}
}