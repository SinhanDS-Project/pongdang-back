package com.wepong.pongdang.model.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.wepong.pongdang.config.AmazonS3Config;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class S3FileService {

	@Autowired
	AmazonS3Client amazonS3; // 앞서 설정한 AmazonS3 빈 주입

	@Autowired
	private AmazonS3Config amazonS3Config;

	private String bucketName;
	private String region;

	@PostConstruct
	public void init() {
		this.bucketName = amazonS3Config.getBucketName();
		this.region = amazonS3Config.getRegion();
	}

	// 단일 파일 업로드
	public String uploadFile(MultipartFile file) {
		if (file.isEmpty()) return null;
		
		// 파일 이름, 확장자
		String originalName = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8);
		String extension = originalName.substring(originalName.lastIndexOf("."));
		
		// 파일명: UUID + 확장자 형태로 지정
	    String key = UUID.randomUUID() + extension;

		try (InputStream is = file.getInputStream()) {
			
			ObjectMetadata metadata = new ObjectMetadata();
			
			metadata.setContentLength(file.getSize());
			metadata.setContentType(file.getContentType());
			
			// S3에 파일 업로드
			amazonS3.putObject(bucketName, key, is, metadata);
		} catch (IOException e) {
			throw new RuntimeException("S3 파일 업로드 실패", e);
		}
		
		// 업로드 후 URL 생성
        return key;
	}

	// 다중 파일 업로드
	public List<String> uploadFiles(MultipartFile[] files) {
		List<String> uploadedKeys = new ArrayList<>();
		for (MultipartFile file : files) {
			String key = uploadFile(file);
			if (key != null) {
				uploadedKeys.add(key);
			}
		}
		return uploadedKeys;
	}
	
	// summernote에서 이미지 삭제 시 S3 객체의 전체 URL을 받아 object key 파싱 
	public void deleteFileByUrl(String fileUrl) {
		// fileUrl이 null이 아니고, 우리가 기대하는 S3 호스트로 시작하는지 확인
	    String prefix = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
	    // prefix 이후의 부분만 잘라내면 버킷 내 object key가 됨
	    if (fileUrl != null && fileUrl.startsWith(prefix)) {
	        String key = fileUrl.substring(prefix.length());
	    // 잘라낸 key를 이용해 S3에서 실제 객체를 삭제
	        deleteObject(key);
	    }
	}
	
	// S3에서 파일 삭제
    public void deleteObject(String objectKey) {
    	
        try {
            // S3 버킷에서 객체 삭제 요청
        	amazonS3.deleteObject(bucketName, objectKey);
        } catch (AmazonServiceException e) {
            // AWS S3 측에서 오류 응답을 보내온 경우 (예: 권한 부족, 버킷/객체 없음 등)
            // 요청은 성공적으로 전송되었으나 S3에서 처리하지 못하고 에러를 반환한 상황
            System.err.println("S3 서비스 오류: " + e.getMessage());
        } catch (SdkClientException e) {
            // 클라이언트 측 오류가 발생한 경우 (예: 네트워크 문제, 인증 오류 등)
            // S3에 요청이 도달하지 못했거나, 응답을 처리하지 못한 상황
            System.err.println("클라이언트 오류: " + e.getMessage());
        }
    }
}
