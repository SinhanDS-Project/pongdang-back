package com.wepong.pongdang.model.email;

public final class VerificationTemplate {

    private VerificationTemplate() {}

    /** 이메일 본문 HTML 생성 */
    public static String render(String vNumber) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>Email Verification</title>
            </head>
            <body>
                <div>
                <table width="670" cellspacing="0" cellpadding="0" align="center">
                    <tbody>
                    <tr>
                        <td style="background-color:#00236e;color:white;padding:32px;font-size:28px;font-weight:700">
                            퐁당퐁당
                        </td>
                    </tr>
                    <tr style="background-color:#fff">
                        <td style="padding:32px">
                        <div style="font-size:24px;font-weight:700">
                              인증번호 확인 후<br>
                              이메일 인증을 완료해주세요.
                        </div>
                        <hr style="margin:32px 0;border-color:#d9d9d9">
                        <div style="font-size:18px;color:#000;margin-bottom:20px">
                              안녕하세요 퐁당퐁당 서비스입니다.<br>
                              아래 인증번호를 입력하여 이메일 인증을 완료해주세요.<br>
                              인증번호 유효시간은 5분입니다.
                        </div>
                        <div style="font-size:18px;font-weight:700;margin-bottom:10px">
                              인증번호 : <span style="font-weight:bold;color:#0046ff">%s</span>
                        </div>
                        <hr style="margin:32px 0 0 0;border-color:#d9d9d9">
                        <div style="margin-top:10px;font-size:12px;line-height:18px;letter-spacing:-1px;color:#767676">
                          본 메일은 발신전용입니다.<br>
                          Copyright © PongDang.
                          All rights reserved.
                        </div>
                        </td>
                    </tr>
                    <tr style="height:40px">
                      <td></td>
                    </tr>
                    </tbody>
                </table>
                </div>
            </body>
            </html>
            """, vNumber);
    }
}
