package com.budaos.support.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.qrcode.BufferedImageLuminanceSource;
import com.budaos.support.domain.dto.QRCodeGenerateRequest;
import com.budaos.support.domain.dto.QRCodeResponse;
import com.budaos.support.exception.QRCodeException;
import com.budaos.support.service.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.Writer;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码服务实现
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
@Slf4j
@Service
public class QRCodeServiceImpl implements QRCodeService {

    private static final int DEFAULT_SIZE = 300;
    private static final int MIN_SIZE = 50;
    private static final int MAX_SIZE = 2000;
    private static final String DEFAULT_FOREGROUND = "#000000";
    private static final String DEFAULT_BACKGROUND = "#FFFFFF";

    @Override
    public QRCodeResponse generateQRCode(QRCodeGenerateRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 验证尺寸
            validateSize(request.getSize());

            // 设置默认颜色
            String fgColor = StrUtil.isNotBlank(request.getForegroundColor()) ?
                    request.getForegroundColor() : DEFAULT_FOREGROUND;
            String bgColor = StrUtil.isNotBlank(request.getBackgroundColor()) ?
                    request.getBackgroundColor() : DEFAULT_BACKGROUND;

            // 生成二维码
            String qrCodeBase64;
            if (StrUtil.isNotBlank(request.getLogoBase64())) {
                // 带Logo的二维码
                qrCodeBase64 = generateQRCodeWithLogo(
                        request.getContent(),
                        request.getLogoBase64(),
                        request.getSize(),
                        fgColor,
                        bgColor,
                        request.getLogoSize()
                );
            } else {
                // 普通二维码
                qrCodeBase64 = generateQRCodeBase64(
                        request.getContent(),
                        request.getSize(),
                        fgColor,
                        bgColor
                );
            }

            // 构建响应
            return QRCodeResponse.builder()
                    .qrCodeBase64(qrCodeBase64)
                    .generateTimeMs(System.currentTimeMillis() - startTime)
                    .fileSize(calculateFileSize(qrCodeBase64))
                    .imageFormat("PNG")
                    .build();

        } catch (Exception e) {
            log.error("生成二维码失败，内容: {}", StrUtil.sub(request.getContent(), 0, 50), e);
            throw new QRCodeException("生成二维码失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String decodeQRCode(String base64Image) {
        if (StrUtil.isBlank(base64Image)) {
            throw new QRCodeException("二维码图片不能为空");
        }

        try {
            // 验证Base64格式
            if (!base64Image.startsWith("data:image/")) {
                throw new QRCodeException("无效的图片格式，必须是Base64编码的图片");
            }

            // 限制图片大小（5MB）
            if (base64Image.length() > 5 * 1024 * 1024) {
                throw new QRCodeException("二维码图片过大，最大支持5MB");
            }

            // 解析Base64图片
            String pureBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(pureBase64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            // 解析二维码
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(DecodeHintType.TRY_HARDER, true);

            Result result = new MultiFormatReader().decode(bitmap, hints);
            return result.getText();

        } catch (QRCodeException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析二维码失败，图片长度: {}", base64Image.length(), e);
            throw new QRCodeException("解析二维码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成二维码Base64
     */
    private String generateQRCodeBase64(String content, int size, String fgColor, String bgColor) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        // 创建自定义颜色的BufferedImage
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Color foreground = Color.decode(fgColor);
        Color background = Color.decode(bgColor);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? foreground.getRGB() : background.getRGB());
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * 生成带Logo的二维码
     */
    private String generateQRCodeWithLogo(String content, String logoBase64, int size,
                                         String fgColor, String bgColor, Integer logoSize) throws Exception {
        // 生成基础二维码
        String baseQRCode = generateQRCodeBase64(content, size, fgColor, bgColor);

        // 解析Base64图片
        String pureBase64 = baseQRCode.substring(baseQRCode.indexOf(",") + 1);
        byte[] qrCodeBytes = Base64.getDecoder().decode(pureBase64);
        BufferedImage qrCodeImage = ImageIO.read(new ByteArrayInputStream(qrCodeBytes));

        // 解析Logo
        String pureLogoBase64 = logoBase64.substring(logoBase64.indexOf(",") + 1);
        byte[] logoBytes = Base64.getDecoder().decode(pureLogoBase64);
        BufferedImage logoImage = ImageIO.read(new ByteArrayInputStream(logoBytes));

        // 计算Logo尺寸（默认为二维码的1/5）
        int actualLogoSize = (logoSize != null && logoSize > 0) ? logoSize : size / 5;

        // 缩放Logo
        BufferedImage scaledLogo = new BufferedImage(actualLogoSize, actualLogoSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledLogo.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(logoImage, 0, 0, actualLogoSize, actualLogoSize, null);
        g.dispose();

        // 将Logo绘制到二维码中心
        Graphics2D graphics = qrCodeImage.createGraphics();
        int logoX = (size - actualLogoSize) / 2;
        int logoY = (size - actualLogoSize) / 2;
        graphics.drawImage(scaledLogo, logoX, logoY, null);
        graphics.dispose();

        // 输出为Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(qrCodeImage, "PNG", outputStream);

        return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * 验证尺寸
     */
    private void validateSize(Integer size) {
        if (size == null) {
            size = DEFAULT_SIZE;
        }

        if (size < MIN_SIZE) {
            throw new QRCodeException("二维码尺寸太小，最小为" + MIN_SIZE + "px");
        }
        if (size > MAX_SIZE) {
            throw new QRCodeException("二维码尺寸太大，最大为" + MAX_SIZE + "px");
        }
    }

    /**
     * 计算文件大小
     */
    private int calculateFileSize(String base64) {
        String pureBase64 = base64.contains(",") ?
                base64.substring(base64.indexOf(",") + 1) : base64;
        return (pureBase64.length() * 3) / 4;
    }
}
