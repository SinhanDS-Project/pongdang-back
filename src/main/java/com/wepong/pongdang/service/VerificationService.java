package com.wepong.pongdang.service;

import com.wepong.pongdang.entity.EmailVerificationEntity;
import com.wepong.pongdang.model.email.TempPasswordTemplate;
import com.wepong.pongdang.model.email.VerificationTemplate;
import com.wepong.pongdang.repository.VerificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationService {

	private final VerificationRepository verificationRepository;
	private final JavaMailSender mailSender;
	private final AuthService authService;

	// 인증 요청 시
	// @Async
	public void requestVerification(String email) throws MessagingException {
		// 인증 코드 생성
		String code = generateVerificationCode();

		// 코드 유효 시간(5분)
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 5);

		EmailVerificationEntity verification = EmailVerificationEntity.builder()
			.email(email)
			.verificationCode(code)
			.expiredAt(new Timestamp(cal.getTimeInMillis()))
			.isVerified(false)
			.build();

		verificationRepository.save(verification);

		sendEmail(email, code);
	}

	// 임시 비밀번호 발급
	// @Async
	public void updatePassword(String email, Long userId) throws MessagingException {
		String tempPassword = generateTempPassword();

		authService.updatePassword(userId, tempPassword);

		sendTempPassword(email, tempPassword);
	}

	// 임시 비밀번호 이메일 발송
	private void sendTempPassword(String email, String tempPassword) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setTo(email);
		helper.setSubject("[퐁당퐁당] 임시 비밀번호 발급 안내");

		String htmlContent = TempPasswordTemplate.render(tempPassword);
		helper.setText(htmlContent, true);

		mailSender.send(message);
	}

	// 임시 비밀번호 생성
	private String generateTempPassword() {
		String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lower = "abcdefghijklmnopqrstuvwxyz";
		String digits = "0123456789";
		String special = "!@#$%^&*";
		String all = upper + lower + digits + special;
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder();

		// 각 문자군에서 1개씩 무조건 포함
		sb.append(upper.charAt(rnd.nextInt(upper.length())));
		sb.append(lower.charAt(rnd.nextInt(lower.length())));
		sb.append(digits.charAt(rnd.nextInt(digits.length())));
		sb.append(special.charAt(rnd.nextInt(special.length())));

		// 나머지 4자리는 전체에서 랜덤
		for (int i = 0; i < 4; i++) {
			sb.append(all.charAt(rnd.nextInt(all.length())));
		}

		// 문자 섞기
		char[] password = sb.toString().toCharArray();
		for (int i = password.length - 1; i > 0; i--) {
			int j = rnd.nextInt(i + 1);
			char tmp = password[i];
			password[i] = password[j];
			password[j] = tmp;
		}

		return new String(password);
	}

	// 인증번호 확인
	public boolean verifyCode(String email, String code) {
		EmailVerificationEntity verification = verificationRepository.findById(email).orElseThrow(() -> new RuntimeException("인증요청이 없습니다."));

		if(verification.getExpiredAt().before(new java.util.Date())) {
			throw new RuntimeException("인증번호가 만료되었습니다.");
		}

		if(!verification.getVerificationCode().equals(code)) {
			throw new RuntimeException("인증번호가 일치하지 않습니다.");
		}

		verification.markVerified();

		verificationRepository.save(verification);

		return true;
	}

	private void sendEmail(String email, String code) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setTo(email);
		helper.setSubject("[퐁당퐁당] 이메일 인증번호 안내");

		String htmlContent = VerificationTemplate.render(code);
		helper.setText(htmlContent, true);

		mailSender.send(message);
	}

	private String generateVerificationCode() {
		Random random = new Random();
		int code = 100000 + random.nextInt(900000); // 6자리 숫자
		return String.valueOf(code);
	}
}
