package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.request.BannerRequestDTO;
import com.wepong.pongdang.dto.request.ChatLogRequestDTO;
import com.wepong.pongdang.dto.response.ChatLogResponseDTO;
import com.wepong.pongdang.entity.BannerEntity;
import com.wepong.pongdang.entity.ChatLogsEntity;
import com.wepong.pongdang.model.aws.S3FileService;
import com.wepong.pongdang.repository.BannerRepository;
import com.wepong.pongdang.repository.ChatLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BannerRepository bannerRepository;
    private final ChatLogRepository chatLogRepository;
    private S3FileService s3FileService;

    // 배너 등록
    @Transactional
    public void insertBanner(BannerRequestDTO banner, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("배너 이미지는 필수입니다.");
        }

        // S3 업로드 (URL 반환 가정)
        String imgUrl = s3FileService.uploadFile(file);

        // 3) 엔티티 빌드 후 저장
        BannerEntity entity = BannerEntity.builder()
                .title(banner.getTitle())
                .imagePath(imgUrl)
                .bannerLinkUrl(banner.getBannerLinkUrl())
                .description(banner.getDescription())
                .build();

        bannerRepository.save(entity);
    }

    // 문의 내역 전체 조회
    public List<ChatLogResponseDTO.ChatLogDetailDTO> selectAllChatLogs() {
        return chatLogRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(ChatLogResponseDTO.ChatLogDetailDTO::from)
                .toList();
    }

    // 문의 답변
    public void insertChatLogAnswer(Long chatLogId, ChatLogRequestDTO.ChatLogAnswerRequestDTO req) {
        ChatLogsEntity chatLog = chatLogRepository.findById(chatLogId)
                .orElseThrow(() -> new EntityNotFoundException("문의를 찾을 수 없습니다."));

        chatLog.setResponse(req.getResponse());
        chatLog.setResponseDate(LocalDateTime.now());

        chatLogRepository.save(chatLog);
    }

}
