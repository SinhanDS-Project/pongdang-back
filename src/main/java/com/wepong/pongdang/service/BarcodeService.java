package com.wepong.pongdang.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.wepong.pongdang.config.AmazonS3Config;
import com.wepong.pongdang.dto.response.ProductResponseDTO;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.model.product.EmailTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BarcodeService {

    private final JavaMailSender mailSender;
    private final AuthService authService;
    private final StoreService storeService;
    private final AmazonS3Config amazonS3Config;


    public void generateBarcode(Long userId, Long productId) throws IOException, WriterException, MessagingException {
        UserEntity user = authService.findById(userId);
        String email = user.getEmail();

        String barcodeValue = generateRandomBarcodeValue();
        byte[] barcodeImage = generateBarcodeImage(barcodeValue, 300, 100);
        sendBarcodeEmail(email, barcodeValue, barcodeImage, productId);
    }

    private String generateRandomBarcodeValue() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 12; i++) { // 12자리 숫자
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private byte[] generateBarcodeImage(String barcodeValue, int width, int height) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(barcodeValue, BarcodeFormat.CODE_128, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "png", baos);
        return baos.toByteArray();
    }

    private void sendBarcodeEmail(String email, String barcodeValue, byte[] barcodeImage, Long productId) throws MessagingException, IOException {
        ProductResponseDTO product = storeService.findProductById(productId);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[퐁당퐁당] 주문하신 상품이 도착했습니다!💝");

        String htmlContent = EmailTemplate.render(product.getName(), barcodeValue);

        helper.setText(htmlContent, true);

        // 바코드 이미지 첨부
        helper.addInline("barcodeImage", new ByteArrayResource(barcodeImage), "image/png");

        // 상품 기본 이미지(썸네일 등) 첨부
        try {
            if (product.getImg() != null) {
                String key = product.getImg();

                String bucketName = amazonS3Config.getBucketName();
                String region = amazonS3Config.getRegion();
                String fileUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;

                String k = key.toLowerCase();
                String ct = (k.endsWith(".png") ? "image/png" :
                        (k.endsWith(".jpg") ? "image/jpg" : "image/jpeg"));

                URL url = new URL(fileUrl);
                try (InputStream in = url.openStream()) {
                    byte[] imageBytes = in.readAllBytes();
                    helper.addInline("giftImage", new ByteArrayResource(imageBytes), ct);
                }
            }
        } catch (Exception e) {
            log.warn("상품 이미지 인라인 실패: {}", e.getMessage(), e);
        }

        // 로고 이미지 첨부 (예: resources/static/logo.png 에서 불러오기)
        Path path = Paths.get("src/main/resources/static/images/pongdang.png");
        byte[] logoBytes = Files.readAllBytes(path);
        helper.addInline("logoImage", new ByteArrayResource(logoBytes), "image/png");

        mailSender.send(message);
    }

}
