package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.request.DonationRequestDTO;
import com.wepong.pongdang.dto.response.DonationInfoResponseDTO;
import com.wepong.pongdang.dto.response.DonationResponseDTO;
import com.wepong.pongdang.entity.DonationInfoEntity;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.WalletType;
import com.wepong.pongdang.entity.mapping.DonationEntity;
import com.wepong.pongdang.repository.DonationInfoRepository;
import com.wepong.pongdang.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationInfoRepository donationInfoRepository;
    private final DonationRepository donationRepository;
    private final AuthService authService;
    private final WalletService walletService;
    private final HistoryService historyService;
    ModelMapper modelMapper = new ModelMapper();

    // 기부 정보 리스트 조회(페이징)
    public Page<DonationInfoResponseDTO> findInfoAll(int page, int size) {
        // 페이징 처리
        PageRequest pageRequest = PageRequest.of(page-1, size, Sort.Direction.DESC, "startDate");
        Page<DonationInfoEntity> infoList = donationInfoRepository.findAll(pageRequest);
        Page<DonationInfoResponseDTO> responseList = infoList.map(entity -> modelMapper.map(entity, DonationInfoResponseDTO.class));

        return responseList;
    }

    // 기부 정보 상세 조회
    public DonationInfoResponseDTO findInfoById(Long infoId) {
        DonationInfoEntity entity = donationInfoRepository.findById(infoId).orElseThrow(() -> new RuntimeException("기부 정보가 존재하지 않습니다."));
        return modelMapper.map(entity, DonationInfoResponseDTO.class);
    }

    // 기부
    @Transactional
    public DonationResponseDTO pongDonate(DonationRequestDTO request, Long userId) {
        DonationInfoEntity donationInfo = donationInfoRepository.findById(request.getDonationInfoId()).orElseThrow(() -> new RuntimeException("기부 정보가 존재하지 않습니다."));
        UserEntity user = authService.findById(userId);
        WalletType walletType = request.getWalletType();
        PongHistoryType historyType = null;

        // 타입 별 지갑에서 차감
        walletService.lose(request.getAmount(), userId, walletType);

        if(walletType.equals(WalletType.PONG)) {
            historyType = PongHistoryType.DONATION_P;
        } else if(walletType.equals(WalletType.DONA)) {
            historyType = PongHistoryType.DONATION_D;
        }

        // 퐁 내역 저장
        PongHistoryEntity pongHistory = PongHistoryEntity.builder()
                .amount(request.getAmount())
                .type(historyType)
                .user(user)
                .build();

        historyService.insertPointHistory(pongHistory, userId);

        // 기부 모금액 증가
        Long current = donationInfo.getCurrent() != null ? donationInfo.getCurrent() : 0L;
        donationInfo.setCurrent(current + request.getAmount());
        donationInfoRepository.save(donationInfo);

        // 기부 내역 저장
        DonationEntity donation = DonationEntity.builder()
                .donationInfo(donationInfo)
                .amount(request.getAmount())
                .user(user)
                .build();

        donationRepository.save(donation);

        return DonationResponseDTO.from(donation);
    }

    // 기부 현황
    public DonationResponseDTO.Status status() {
        // donation의 amount의 합
        List<DonationEntity> donations = donationRepository.findAll();
        int totalAmount = donations.stream()
                .mapToInt(DonationEntity::getAmount)
                .sum();
        // donation의 개수
        Long totalCount = donationRepository.count();

        DonationResponseDTO.Status status = DonationResponseDTO.Status.builder()
                .totalCount(totalCount)
                .totalAmount(totalAmount)
                .build();

        return status;
    }

    // 사용자 기부 내역 조회(페이징)
    public Page<DonationResponseDTO> findDonationByUserId(int page, int size, Long userId) {
        PageRequest pageRequest = PageRequest.of(page-1, size, Sort.Direction.DESC, "createdAt");
        Page<DonationEntity> donaList = donationRepository.findByUserId(userId, pageRequest);
        Page<DonationResponseDTO> responseList = donaList.map(entity -> DonationResponseDTO.from(entity));

        return responseList;
    }
}
