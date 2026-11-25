package com.example.shortlab.application;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

/**
 * 주어진 문자열(단축 URL)을 QR 코드 SVG로 만든다.
 * SVG는 확대해도 깨지지 않아서 인쇄물에 쓰기 좋다.
 * 길이 단위는 "QR 모듈(점 하나)"이고, 실제 표시 크기는 viewBox가 맞춰 준다.
 */
@Service
public class QrCodeService {

    private static final int QUIET_ZONE = 4;   // QR 바깥 여백 (스캔 인식에 필요한 최소 여백)
    private static final int SCALE = 10;       // 기본 표시 크기 = 모듈 수 x 10px
    private static final String FOREGROUND = "111827";
    private static final String BACKGROUND = "ffffff";

    public String generateSvg(String content) {
        BitMatrix matrix = encode(content);
        int modules = matrix.getWidth();
        int size = modules + QUIET_ZONE * 2;

        StringBuilder svg = new StringBuilder(16 * 1024);
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
                .append(size).append(' ').append(size)
                .append("\" width=\"").append(size * SCALE)
                .append("\" height=\"").append(size * SCALE).append("\">");

        // 배경
        svg.append("<rect width=\"").append(size).append("\" height=\"").append(size)
                .append("\" fill=\"#").append(BACKGROUND).append("\"/>");

        // 모듈: 하나의 path로 그려서 점 사이에 가는 틈(앤티앨리어싱 선)이 생기지 않게 한다.
        svg.append("<path fill=\"#").append(FOREGROUND).append("\" d=\"");
        for (int y = 0; y < modules; y++) {
            for (int x = 0; x < modules; x++) {
                if (matrix.get(x, y)) {
                    svg.append('M').append(x + QUIET_ZONE).append(' ').append(y + QUIET_ZONE)
                            .append("h1v1h-1z");
                }
            }
        }
        svg.append("\"/></svg>");

        return svg.toString();
    }

    private BitMatrix encode(String content) {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 여백은 SVG에서 직접 그리므로 ZXing의 여백은 0으로 둔다.
        hints.put(EncodeHintType.MARGIN, 0);

        try {
            // 크기를 0으로 주면 "모듈 1개 = 1칸"인 최소 크기 행렬이 만들어진다.
            return new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints);
        } catch (WriterException e) {
            throw new IllegalStateException("QR 코드를 생성하지 못했습니다.", e);
        }
    }
}