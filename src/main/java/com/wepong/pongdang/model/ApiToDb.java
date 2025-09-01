package com.wepong.pongdang.model;

import com.wepong.pongdang.entity.DonationInfoEntity;
import com.wepong.pongdang.repository.DonationInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiToDb implements ApplicationRunner { // 서버 실행 시 한 번 실행

    private final DonationInfoRepository donationInfoRepository;

    @Value("${data-go.api-url}")
    private String apiUrl;

    @Value("${data-go.service-key}")
    private String serviceKey;

    // API에서 오는 날짜 형식과 일치하도록 포맷터 정의
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 필터링 조건
    private static final List<String> VALID_TYPES = List.of("보건복지", "시민사회구축", "자선", "재난구휼", "환경보전");
    private static final LocalDateTime END_DATE_THRESHOLD = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        donationInfoRepository.deleteAll();

        int pageNo = 1;
        int numOfRows = 100;
        int totalCount = -1;

        while (true) {
            StringBuilder urlBuilder = new StringBuilder(apiUrl);
            urlBuilder.append("?serviceKey=").append(URLEncoder.encode(serviceKey, "UTF-8"));
            urlBuilder.append("&pageNo=").append(pageNo);
            urlBuilder.append("&numOfRows=").append(numOfRows);
            urlBuilder.append("&step=").append("1");

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (InputStream is = (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 299)
                    ? conn.getInputStream() : conn.getErrorStream()) {

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(false);
                dbf.setIgnoringComments(true);
                dbf.setIgnoringElementContentWhitespace(true);

                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(new InputStreamReader(is, "UTF-8")));
                doc.getDocumentElement().normalize();

                String resultCode = getText(doc, "resultCode");
                if (!"00".equals(resultCode)) {
                    String msg = getText(doc, "resultMsg");
                    throw new RuntimeException("API error: " + resultCode + " / " + msg);
                }

                if (totalCount < 0) {
                    String tc = getText(doc, "totalCount");
                    totalCount = (tc == null || tc.isEmpty()) ? -1 : Integer.parseInt(tc);
                }

                NodeList items = doc.getElementsByTagName("item");

                // 일괄 저장을 위한 리스트
                List<DonationInfoEntity> donationsToSave = new java.util.ArrayList<>();

                for (int i = 0; i < items.getLength(); i++) {
                    Element it = (Element) items.item(i);

                    String title   = getText(it, "reprsntSj");
                    String content = getText(it, "rcritSj");
                    String purpose = getText(it, "rcritPurps");
                    String org     = getText(it, "rcritrNm");
                    String start   = getText(it, "rcritBgnde");
                    String end     = getText(it, "rcritEndde");
                    String goal    = getText(it, "rcritGoalAm");
                    String cate    = getText(it, "cntrClUpNm");

                    // 문자열 데이터를 DonationInfoEntity에 맞게 변환
                    LocalDateTime startDate = parseDate(start);
                    LocalDateTime endDate = parseDate(end);
                    Long goalAmount = parseLong(goal);

                    // 필터링 적용
                    if(!VALID_TYPES.contains(cate)) continue;
                    if(endDate == null || !endDate.isAfter(END_DATE_THRESHOLD)) continue;

                    // 기부 정보 엔티티 생성
                    DonationInfoEntity donation = DonationInfoEntity.builder()
                            .title(Optional.ofNullable(title).orElse(""))
                            .content(Optional.ofNullable(content).orElse(""))
                            .purpose(Optional.ofNullable(purpose).orElse(""))
                            .org(Optional.ofNullable(org).orElse(""))
                            .startDate(startDate)
                            .endDate(endDate)
                            .type(cate)
                            .goal(goalAmount)
                            .build();

                    donationsToSave.add(donation);
                }

                // 리스트에 모아둔 엔티티를 한 번에 저장 (배치 처리)
                if (!donationsToSave.isEmpty()) {
                    donationInfoRepository.saveAll(donationsToSave);
                }

                int got = items.getLength();

                if (totalCount > -1) {
                    int totalPages = (int) Math.ceil(totalCount / (double) numOfRows);
                    if (pageNo >= totalPages) break;
                } else {
                    if (got < numOfRows) break;
                }
                pageNo++;
                Thread.sleep(150);
            }
        }
    }

    // 날짜 문자열을 LocalDateTime으로 파싱하는 헬퍼 메서드
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        // LocalDate로 먼저 파싱
        LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        // LocalDateTime으로 변환 (자정 기준)
        return localDate.atStartOfDay();
    }

    // 금액 문자열을 Long으로 파싱하는 헬퍼 메서드
    private Long parseLong(String longStr) {
        if (longStr == null || longStr.isEmpty()) {
            return 0L; // 또는 기본값 설정
        }
        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    static String getText(Document doc, String tag) {
        NodeList nl = doc.getElementsByTagName(tag);
        return (nl.getLength() == 0) ? "" : nl.item(0).getTextContent();
    }

    static String getText(Element el, String tag) {
        NodeList nl = el.getElementsByTagName(tag);
        return (nl.getLength() == 0) ? "" : nl.item(0).getTextContent();
    }
}