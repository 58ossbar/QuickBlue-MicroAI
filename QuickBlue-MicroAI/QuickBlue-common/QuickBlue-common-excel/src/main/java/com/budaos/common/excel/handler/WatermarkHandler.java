package com.budaos.common.excel.handler;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

/**
 * Excel 水印处理器
 * <p>
 * 在导出的 Excel 中添加自定义水印，支持水印内容、颜色、字体、角度等自定义配置
 * </p>
 *
 * @author QuickBlue
 * @since 4.0.0
 */
public class WatermarkHandler implements SheetWriteHandler {

    private static final Logger log = LoggerFactory.getLogger(WatermarkHandler.class);

    private final Watermark watermark;

    public WatermarkHandler(Watermark watermark) {
        this.watermark = watermark;
    }

    /**
     * 使用默认配置创建水印处理器
     *
     * @param content 水印内容
     */
    public WatermarkHandler(String content) {
        this(new Watermark(content));
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        BufferedImage bufferedImage = createWatermarkImage();
        XSSFWorkbook workbook = (XSSFWorkbook) writeSheetHolder.getParentWriteWorkbookHolder().getWorkbook();
        try {
            // 添加水印的具体操作
            addWatermarkToSheet(workbook, bufferedImage);
        } catch (Exception e) {
            log.error("添加水印出错:", e);
        }
    }

    /**
     * 创建水印图片
     *
     * @return 水印图片
     */
    private BufferedImage createWatermarkImage() {
        // 获取水印相关参数
        Font font = watermark.getFont();
        int width = watermark.getWidth();
        int height = watermark.getHeight();
        Color color = watermark.getColor();
        String text = watermark.getContent();

        // 创建带有透明背景的 BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // 设置画笔字体、平滑、颜色
        g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);

        // 计算水印位置和角度
        int y = watermark.getYAxis();
        int x = watermark.getXAxis();
        AffineTransform transform = AffineTransform.getRotateInstance(
                Math.toRadians(-watermark.getAngle()), 0, y);
        g.setTransform(transform);
        // 绘制水印文字
        g.drawString(text, x, y);

        // 释放资源
        g.dispose();

        return image;
    }

    /**
     * 添加水印到 Sheet
     *
     * @param workbook       工作簿
     * @param watermarkImage 水印图片
     */
    private void addWatermarkToSheet(XSSFWorkbook workbook, BufferedImage watermarkImage) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(watermarkImage, "png", os);
            int pictureIdx = workbook.addPicture(os.toByteArray(), XSSFWorkbook.PICTURE_TYPE_PNG);
            XSSFPictureData pictureData = workbook.getAllPictures().get(pictureIdx);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                // 获取每个Sheet表
                XSSFSheet sheet = workbook.getSheetAt(i);
                PackagePartName ppn = pictureData.getPackagePart().getPartName();
                String relType = XSSFRelation.IMAGES.getRelation();
                PackageRelationship pr = sheet.getPackagePart().addRelationship(ppn, TargetMode.INTERNAL, relType, null);
                sheet.getCTWorksheet().addNewPicture().setId(pr.getId());
            }
        } catch (Exception e) {
            log.error("添加水印图片时发生错误", e);
        }
    }

    /**
     * 水印配置类
     */
    public static class Watermark {

        /**
         * 默认画笔颜色
         */
        private static final Color DEFAULT_COLOR = new Color(239, 239, 239);

        /**
         * 默认字体样式
         */
        private static final Font DEFAULT_FONT = new Font("Microsoft YaHei", Font.BOLD, 26);

        /**
         * 默认倾斜角度
         */
        private static final double DEFAULT_ANGLE = 25;

        /**
         * 水印内容
         */
        private final String content;

        /**
         * 画笔颜色
         */
        private Color color = DEFAULT_COLOR;

        /**
         * 字体样式
         */
        private Font font = DEFAULT_FONT;

        /**
         * 水印宽度
         */
        private int width;

        /**
         * 水印高度
         */
        private int height;

        /**
         * 倾斜角度（非弧度制）
         */
        private double angle = DEFAULT_ANGLE;

        /**
         * 字体的y轴位置
         */
        private int yAxis;

        /**
         * 字体的X轴位置
         */
        private int xAxis;

        /**
         * 水平倾斜度
         */
        private double shearX = 0.1;

        /**
         * 垂直倾斜度
         */
        private double shearY = -0.26;

        /**
         * 构造函数 - 使用默认配置
         *
         * @param content 水印内容
         */
        public Watermark(String content) {
            this.content = content;
            init();
        }

        /**
         * 构造函数 - 完全自定义配置
         *
         * @param content 水印内容
         * @param color   颜色
         * @param font    字体
         * @param angle   角度
         */
        public Watermark(String content, Color color, Font font, double angle) {
            this.content = content;
            this.color = color;
            this.font = font;
            this.angle = angle;
            init();
        }

        /**
         * 根据水印内容长度自适应水印图片大小
         * 使用简单的三角函数计算
         */
        private void init() {
            // 创建临时图像用于获取字体度量
            BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = tempImage.createGraphics();
            FontMetrics fontMetrics = g2d.getFontMetrics(this.font);
            g2d.dispose();

            int stringWidth = fontMetrics.stringWidth(this.content);
            int charWidth = fontMetrics.charWidth('A');
            this.width = (int) Math.abs(stringWidth * Math.cos(Math.toRadians(this.angle))) + 5 * charWidth;
            this.height = (int) Math.abs(stringWidth * Math.sin(Math.toRadians(this.angle))) + 5 * charWidth;
            this.yAxis = this.height;
            this.xAxis = charWidth;
        }

        // Getters and Setters

        public String getContent() {
            return content;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            this.font = font;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public double getAngle() {
            return angle;
        }

        public void setAngle(double angle) {
            this.angle = angle;
        }

        public int getYAxis() {
            return yAxis;
        }

        public void setYAxis(int yAxis) {
            this.yAxis = yAxis;
        }

        public int getXAxis() {
            return xAxis;
        }

        public void setXAxis(int xAxis) {
            this.xAxis = xAxis;
        }

        public double getShearX() {
            return shearX;
        }

        public void setShearX(double shearX) {
            this.shearX = shearX;
        }

        public double getShearY() {
            return shearY;
        }

        public void setShearY(double shearY) {
            this.shearY = shearY;
        }
    }
}
