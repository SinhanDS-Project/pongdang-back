package com.wepong.pongdang.model.product;

public final class EmailTemplate {

    private EmailTemplate() {}

    /** 이메일 본문 HTML 생성 */
    public static String render(String productName, String barcodeValue) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset='UTF-8'>
                    <title>Gift Coupon</title>
                </head>
                <body style='margin:0; padding:0; font-family:Arial, sans-serif; background-color:#f0f0f0;'>
                    <p style='font-size:14px; color:#444; margin:12px;'>
                        퐁당퐁당에서 준비한 상품 바코드를 보내드립니다.<br/>
                        소중한 순간에 즐겁게 사용해 보세요! 🎁
                    </p>
                    <div style='width:280px; margin:20px auto; background:#fff; border:20px solid #3dabe1;
                                border-radius:10px; text-align:center; box-shadow:0 4px 12px rgba(0,0,0,0.15);'>
                        <img src='cid:giftImage' alt='Gift Image'
                             style='width:100%%; display:block; border-bottom:1px solid #eee;'/>
                        <h1 style='font-size:20px; margin:15px 0 10px 0; word-break:keep-all;'>%s</h1>
                        <img src='cid:barcodeImage' alt='Barcode Image'
                             style='width:200px; display:block; margin:10px auto;'/>
                        <p style='margin:0; font-size:14px; color:#333;'>%s</p>
                        <div style='margin-top:15px; padding-top:10px; border-top:1px solid #ddd;
                                    display:flex; align-items:center; justify-content:center; gap:6px;'>
                            <img src='cid:logoImage' alt='Logo' style='width:40px; height:auto;'/>
                            <span style='font-size:18px; font-weight:bold; color:#222;'>퐁당퐁당</span>
                        </div>
                    </div>
                </body>
                </html>
                """, productName, barcodeValue);
    }
}
