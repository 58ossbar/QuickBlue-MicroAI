package com.budaos.common.web.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

/**
 * Excel导出时插入二维码图片的自定义处理器
 *
 * @author budaos
 * @since 2026-02-11
 */
@Slf4j
public class CustomImageWriteHandler implements CellWriteHandler {

    /**
     * 二维码列索引（从0开始计数，22表示第23列）
     * 可以在构造函数中指定
     */
    private final int qrCodeColumnIndex;

    /**
     * 二维码标准大小（像素）
     */
    private static final int QR_CODE_SIZE_PIXELS = 80;

    /**
     * Excel宽度单位转换常量
     */
    private static final double PIXELS_TO_EXCEL_WIDTH_UNITS = 256.0 / 8.0;

    /**
     * 每英寸点数
     */
    private static final double POINTS_PER_INCH = 72.0;

    /**
     * 屏幕DPI
     */
    private static final int DPI = 96;

    /**
     * 默认构造函数：使用默认的第22列（索引21）
     */
    public CustomImageWriteHandler() {
        this(21);
    }

    /**
     * 自定义构造函数：指定二维码列索引
     *
     * @param qrCodeColumnIndex 二维码列索引（从0开始）
     */
    public CustomImageWriteHandler(int qrCodeColumnIndex) {
        this.qrCodeColumnIndex = qrCodeColumnIndex;
    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                   List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {

        // 只在非表头行且是指定二维码列时处理
        if (isHead != null && !isHead && cell.getColumnIndex() == qrCodeColumnIndex) {
            Sheet sheet = writeSheetHolder.getSheet();
            Workbook workbook = sheet.getWorkbook();

            // 获取单元格的数据
            byte[] imageBytes = null;
            if (cellDataList != null && !cellDataList.isEmpty()) {
                Object cellValue = cellDataList.get(0).getData();
                if (cellValue instanceof byte[]) {
                    imageBytes = (byte[]) cellValue;
                }
            }

            // 如果有图片数据，插入图片
            if (imageBytes != null && imageBytes.length > 0) {
                try {
                    // 清除单元格内容
                    cell.setCellValue("");

                    // 创建图片
                    int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

                    // 创建绘图工具
                    CreationHelper helper = workbook.getCreationHelper();
                    Drawing<?> drawing = sheet.createDrawingPatriarch();

                    // 设置图片位置和大小
                    ClientAnchor anchor;
                    if (workbook instanceof XSSFWorkbook) {
                        anchor = new XSSFClientAnchor();
                    } else {
                        anchor = helper.createClientAnchor();
                    }

                    // 设置图片位置（相对于单元格）
                    int colIndex = cell.getColumnIndex();
                    int rowIndex = cell.getRowIndex();

                    anchor.setCol1(colIndex);
                    anchor.setRow1(rowIndex);
                    anchor.setCol2(colIndex + 1); // 占用一列
                    anchor.setRow2(rowIndex + 1); // 占用一行

                    // 计算图片位置偏移（居中显示）
                    int imageWidthUnits = Units.pixelToEMU(QR_CODE_SIZE_PIXELS);
                    int imageHeightUnits = Units.pixelToEMU(QR_CODE_SIZE_PIXELS);

                    // 设置图片大小（保持正方形1:1比例）
                    anchor.setDx1(0);
                    anchor.setDy1(0);
                    anchor.setDx2(imageWidthUnits);
                    anchor.setDy2(imageHeightUnits);

                    // 插入图片
                    Picture picture = drawing.createPicture(anchor, pictureIdx);

                    // 设置行高以适应图片
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        // 将像素转换为点（points）
                        double rowHeightInPoints = QR_CODE_SIZE_PIXELS * POINTS_PER_INCH / DPI;
                        row.setHeightInPoints((float) rowHeightInPoints);
                    }

                    // 设置列宽以适应图片（保持正方形比例）
                    int columnWidth = (int) ((QR_CODE_SIZE_PIXELS * PIXELS_TO_EXCEL_WIDTH_UNITS) + 128);
                    sheet.setColumnWidth(colIndex, columnWidth);

                    // 设置图片为随单元格调整大小和移动
                    if (picture != null) {
                        picture.resize();
                    }

                } catch (Exception e) {
                    log.error("二维码图片插入失败，行: {}, 列: {}, 错误: {}",
                            cell.getRowIndex() + 1, cell.getColumnIndex() + 1, e.getMessage(), e);
                    // 如果插入图片失败，显示错误信息
                    cell.setCellValue("图片加载失败");
                }
            } else {
                // 如果没有图片数据，显示提示
                cell.setCellValue("无二维码");
            }
        }
    }
}
