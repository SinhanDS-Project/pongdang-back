package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.request.ChatLogRequestDTO;
import com.wepong.pongdang.entity.ChatLogsEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.repository.ChatLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatLogService {

	private final ChatLogRepository chatLogRepository;
	private final AuthService authService;

	public List<ChatLogsEntity> selectByUser(Long userId) {
		return chatLogRepository.findByUserId(userId);
	}
	
	public Page<ChatLogsEntity> selectByUser(Long userId, int page) {
		int size = 10;
        int offset = (page-1) * size;
		Pageable pageable = PageRequest.of(offset / size, size);
		return chatLogRepository.findByUserId(userId, pageable);
	}
	
	public int chatlogCount(Long userId) {
		return chatLogRepository.countByUserId(userId);
	}	
	
	public ChatLogsEntity selectByUid(Long id) {
		return chatLogRepository.findById(id).orElseThrow(() -> new RuntimeException("채팅이 존재하지 않습니다."));
	}
	
	public ChatLogsEntity insertChatLog(ChatLogRequestDTO.ChatLogQuestionDTO requestChatlog, Long userId) {
		UserEntity userEntity = authService.findById(userId);
		LocalDateTime today = LocalDateTime.now();

		ChatLogsEntity chatLog = ChatLogsEntity.builder()
				.title(requestChatlog.getTitle())
				.question(requestChatlog.getQuestion())
				.chatDate(today)
				.user(userEntity)
				.build();

		return chatLogRepository.save(chatLog);
	}
	
	public void deleteLog(Long id) {
		ChatLogsEntity chat = chatLogRepository.findById(id).orElseThrow(() -> new RuntimeException("채팅이 존재하지 않습니다."));
		chatLogRepository.delete(chat);
	}

}
