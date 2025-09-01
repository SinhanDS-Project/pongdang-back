package com.wepong.pongdang.service;

import com.wepong.pongdang.dto.response.DonationInfoResponseDTO;
import com.wepong.pongdang.entity.DonationInfoEntity;
import com.wepong.pongdang.repository.DonationInfoRepository;
import com.wepong.pongdang.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationInfoRepository donationInfoRepository;
    private final DonationRepository donationRepository;
    ModelMapper modelMapper = new ModelMapper();

    // 기부 정보 리스트 조회(페이징)
    public Page<DonationInfoResponseDTO> findAll(int page, int size) {
        // 페이징 처리
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.DESC, "startDate");
        Page<DonationInfoEntity> infoList = donationInfoRepository.findAll(pageRequest);
        Page<DonationInfoResponseDTO> responseList = infoList.map(entity -> modelMapper.map(entity, DonationInfoResponseDTO.class));

        return responseList;
    }

    // 기부 정보 리스트 조회
    public List<DonationInfoResponseDTO> findAll() {
        List<DonationInfoEntity> infoList = donationInfoRepository.findAll();
        List<DonationInfoResponseDTO> responseList = infoList.stream()
                .map(entity -> modelMapper
                        .map(entity, DonationInfoResponseDTO.class))
                .toList();

        return responseList;
    }

    // 기부 정보 상세 조회
    public DonationInfoResponseDTO findById(Long infoId) {
        DonationInfoEntity entity = donationInfoRepository.findById(infoId).orElseThrow(() -> new RuntimeException("기부 정보가 존재하지 않습니다."));
        return modelMapper.map(entity, DonationInfoResponseDTO.class);
    }
}
