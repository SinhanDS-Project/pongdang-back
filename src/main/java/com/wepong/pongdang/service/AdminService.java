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
import com.wepong.pongdang.dto.request.ProductRequestDTO;
import com.wepong.pongdang.dto.response.DonationInfoResponseDTO;
import com.wepong.pongdang.entity.DonationInfoEntity;
import com.wepong.pongdang.entity.ProductEntity;
import com.wepong.pongdang.repository.DonationInfoRepository;
import com.wepong.pongdang.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BannerRepository bannerRepository;
    private final ChatLogRepository chatLogRepository;
    private final S3FileService s3FileService;
    private final ProductRepository productRepository;
    private final DonationService donationService;
    private final DonationInfoRepository donationInfoRepository;
    ModelMapper modelMapper = new ModelMapper();

    // 배너 등록
    @Transactional
    public void insertBanner(BannerRequestDTO banner, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("배너 이미지는 필수입니다.");
        }

        // S3 업로드 (URL 반환 가정)
        String imgUrl = s3FileService.uploadFile(file);

        //엔티티 빌드 후 저장
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

    // 상품 등록
    @Transactional
    public void saveProduct(ProductRequestDTO request) {
        List<String> keys = s3FileService.uploadFiles(request.getFiles());

        String description = keys.size() > 1 ? keys.get(1) : request.getDescription();
        ProductEntity product = ProductEntity.builder()
                .name(request.getName())
                .type(request.getType())
                .price(request.getPrice())
                .img(keys.get(0))
                .description(description)
                .build();

        productRepository.save(product);
    }

    // 기부 전체 조회
    public List<DonationInfoResponseDTO> findInfoAll() {
        List<DonationInfoEntity> infoList = donationInfoRepository.findAll();
        List<DonationInfoResponseDTO> list = infoList.stream()
                .map(entity -> modelMapper.map(entity, DonationInfoResponseDTO.class))
                .collect(Collectors.toList());

        return list;
    }

    // 기부 수정
    @Transactional
    public void updateDonationInfo(Long infoId, MultipartFile file) {
        String key = s3FileService.uploadFile(file);

        DonationInfoResponseDTO dto = donationService.findInfoById(infoId);
        DonationInfoEntity info = DonationInfoEntity.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .current(dto.getCurrent())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .goal(dto.getGoal())
                .img(key)
                .org(dto.getOrg())
                .purpose(dto.getPurpose())
                .type(dto.getType())
                .build();

        donationInfoRepository.save(info);
    }
}
