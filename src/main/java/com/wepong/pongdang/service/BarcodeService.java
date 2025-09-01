package com.wepong.pongdang.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.wepong.pongdang.entity.UserEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class BarcodeService {

    private final JavaMailSender mailSender;
    private final AuthService authService;

    public void generateBarcode(Long userId) throws IOException, WriterException, MessagingException {
        UserEntity user = authService.findById(userId);
        String email = user.getEmail();

        String barcodeValue = generateRandomBarcodeValue();
        byte[] barcodeImage = generateBarcodeImage(barcodeValue, 300, 100);
        sendBarcodeEmail(email, barcodeValue, barcodeImage);
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

    private void sendBarcodeEmail(String email, String barcodeValue, byte[] barcodeImage) throws MessagingException {
        // 이메일 본문에 이미지 표시 가능
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[퐁당퐁당] 상품 바코드 전송");

        String htmlContent = "<p>"+barcodeValue+"</p>" +
                             "<img src='cid:barcodeImage'/>";

        helper.setText(htmlContent, true);
        helper.addInline("barcodeImage", new ByteArrayResource(barcodeImage), "image/png");

        mailSender.send(message);
    }
}
