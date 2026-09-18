package com.budaos.common.excel.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.budaos.common.excel.domain.ExcelExportDTO;
import com.budaos.common.excel.exception.ExcelExportException;
import com.budaos.common.excel.exception.ExcelImportException;
import com.budaos.common.excel.handler.AutoColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * EasyExcel 工具类
 * <p>
 * 封装 EasyExcel 的常用操作，包括导入导出、样式设置等
 * </p>
 *
 * @author QuickBlue
 * @since 4.0.0
 */
public final class EasyExcelUtil {

    private static final Logger log = LoggerFactory.getLogger(EasyExcelUtil.class);

    /** 默认 Sheet 名称 */
    private static final String DEFAULT_SHEET_NAME = "Sheet1";

    /** Excel 导出响应内容类型 */
    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private EasyExcelUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== 默认样式相关 ====================

    /**
     * 默认表头样式
     *
     * @return 表头样式
     */
    public static WriteCellStyle defaultHeadStyle() {
        WriteCellStyle headStyle = new WriteCellStyle();
        // 设置背景颜色
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        // 设置字体
        WriteFont headFont = new WriteFont();
        headFont.setFontHeightInPoints((short) 12);
        headFont.setBold(true);
        headFont.setColor(IndexedColors.BLACK.getIndex());
        headStyle.setWriteFont(headFont);
        // 设置居中
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return headStyle;
    }

    /**
     * 默认内容样式
     *
     * @return 内容样式
     */
    public static WriteCellStyle defaultContentStyle() {
        WriteCellStyle contentStyle = new WriteCellStyle();
        // 设置居中
        contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 自动换行
        contentStyle.setWrapped(true);

        WriteFont contentFont = new WriteFont();
        contentFont.setFontHeightInPoints((short) 10);
        contentStyle.setWriteFont(contentFont);

        return contentStyle;
    }

    /**
     * 获取默认的样式策略
     *
     * @return 样式策略
     */
    public static HorizontalCellStyleStrategy getDefaultStyleStrategy() {
        return new HorizontalCellStyleStrategy(defaultHeadStyle(), defaultContentStyle());
    }

    // ==================== 导出相关 ====================

    /**
     * 导出 Excel 到 HttpServletResponse（使用默认样式）
     *
     * @param exportDTO 导出数据传输对象
     * @param response  HTTP响应对象
     * @throws ExcelExportException 导出异常
     */
    public static void exportToResponse(ExcelExportDTO exportDTO, HttpServletResponse response)
            throws ExcelExportException {
        exportToResponse(exportDTO, response, getDefaultStyleStrategy());
    }

    /**
     * 导出 Excel 到 HttpServletResponse（自定义样式）
     *
     * @param exportDTO     导出数据传输对象
     * @param response      HTTP响应对象
     * @param styleStrategy 样式策略
     * @throws ExcelExportException 导出异常
     */
    public static void exportToResponse(ExcelExportDTO exportDTO, HttpServletResponse response,
                                        WriteHandler styleStrategy) throws ExcelExportException {
        try {
            // 设置响应头
            setExcelResponseHeader(response, exportDTO.getFileName());

            // 创建 ExcelWriter
            ExcelWriterSheetBuilder sheetBuilder = EasyExcel.write(response.getOutputStream(), exportDTO.getClazz())
                    .autoCloseStream(false) // 不要自动关闭流
                    .sheet(exportDTO.getSheetName() != null ? exportDTO.getSheetName() : DEFAULT_SHEET_NAME);

            // 添加样式策略
            if (styleStrategy != null) {
                sheetBuilder.registerWriteHandler(styleStrategy);
            }

            // 如果不包含表头
            if (!exportDTO.isIncludeHeader()) {
                sheetBuilder.needHead(false);
            }

            // 如果自动调整列宽
            if (exportDTO.isAutoWidth()) {
                sheetBuilder.registerWriteHandler(new AutoColumnWidthStyleStrategy());
            }

            // 写入数据
            sheetBuilder.doWrite(exportDTO.getDataList());

            // 确保输出流完全写入
            response.flushBuffer();

        } catch (Exception e) {
            log.error("导出Excel失败: {}", e.getMessage(), e);
            throw new ExcelExportException("导出Excel失败: " + e.getMessage(), e);
        }
    }

    /**
     * 简化版导出方法
     *
     * @param response  HTTP响应对象
     * @param fileName  文件名
     * @param sheetName Sheet名称
     * @param head      数据类型
     * @param data      数据列表
     * @throws ExcelExportException 导出异常
     */
    public static void export(HttpServletResponse response, String fileName, String sheetName,
                             Class<?> head, Collection<?> data) throws ExcelExportException {
        ExcelExportDTO exportDTO = ExcelExportDTO.builder()
                .dataList(new ArrayList<>(data))
                .fileName(fileName)
                .sheetName(sheetName)
                .clazz(head)
                .includeHeader(true)
                .autoWidth(true)
                .build();

        exportToResponse(exportDTO, response, getDefaultStyleStrategy());
    }

    /**
     * 导出 Excel 到本地文件
     *
     * @param filePath 文件路径
     * @param head     数据类型
     * @param data     数据列表
     * @throws ExcelExportException 导出异常
     */
    public static void exportToFile(String filePath, Class<?> head, List<?> data) throws ExcelExportException {
        exportToFile(filePath, DEFAULT_SHEET_NAME, head, data);
    }

    /**
     * 导出 Excel 到本地文件
     *
     * @param filePath  文件路径
     * @param sheetName Sheet名称
     * @param head      数据类型
     * @param data      数据列表
     * @throws ExcelExportException 导出异常
     */
    public static void exportToFile(String filePath, String sheetName, Class<?> head,
                                   List<?> data) throws ExcelExportException {
        try {
            ExcelWriterSheetBuilder sheetBuilder = EasyExcel.write(filePath, head)
                    .sheet(sheetName);

            sheetBuilder.registerWriteHandler(getDefaultStyleStrategy());
            sheetBuilder.registerWriteHandler(new AutoColumnWidthStyleStrategy());
            sheetBuilder.doWrite(data);
        } catch (Exception e) {
            log.error("导出Excel到文件失败: {}", e.getMessage(), e);
            throw new ExcelExportException("导出Excel到文件失败: " + e.getMessage(), e);
        }
    }

    // ==================== 导入相关 ====================

    /**
     * 导入 Excel 从 InputStream
     *
     * @param inputStream 输入流
     * @param head        数据类型
     * @param <T>         数据类型
     * @return 数据列表
     * @throws ExcelImportException 导入异常
     */
    public static <T> List<T> importExcel(InputStream inputStream, Class<T> head) throws ExcelImportException {
        List<T> dataList = new ArrayList<>();
        try {
            readAll(inputStream, head, createDataCollectListener(dataList));
        } catch (Exception e) {
            log.error("导入Excel失败: {}", e.getMessage(), e);
            throw new ExcelImportException("导入Excel失败: " + e.getMessage(), e);
        }
        return dataList;
    }

    /**
     * 导入 Excel 从文件
     *
     * @param filePath 文件路径
     * @param head     数据类型
     * @param <T>      数据类型
     * @return 数据列表
     * @throws ExcelImportException 导入异常
     */
    public static <T> List<T> importExcelFromFile(String filePath, Class<T> head) throws ExcelImportException {
        List<T> dataList = new ArrayList<>();
        try {
            ExcelReader reader = EasyExcel.read(filePath, head, createDataCollectListener(dataList)).build();
            reader.readAll();
        } catch (Exception e) {
            log.error("从文件导入Excel失败: {}", e.getMessage(), e);
            throw new ExcelImportException("从文件导入Excel失败: " + e.getMessage(), e);
        }
        return dataList;
    }

    /**
     * 导入 Excel 从 MultipartFile
     *
     * @param file 上传文件
     * @param head 数据类型
     * @param <T>  数据类型
     * @return 数据列表
     * @throws ExcelImportException 导入异常
     */
    public static <T> List<T> importExcel(org.springframework.web.multipart.MultipartFile file,
                                          Class<T> head) throws ExcelImportException {
        try {
            return importExcel(file.getInputStream(), head);
        } catch (IOException e) {
            log.error("读取上传文件失败: {}", e.getMessage(), e);
            throw new ExcelImportException("读取上传文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导入 Excel（使用自定义监听器）
     *
     * @param inputStream 输入流
     * @param head        数据类型
     * @param listener    监听器
     * @param <T>         数据类型
     */
    public static <T> void importExcel(InputStream inputStream, Class<T> head,
                                       AnalysisEventListener<T> listener) {
        readAll(inputStream, head, listener);
    }

    /**
     * 使用监听器读取输入流
     */
    private static <T> void readAll(InputStream inputStream, Class<T> head, AnalysisEventListener<T> listener) {
        ExcelReader reader = EasyExcel.read(inputStream, head, listener).build();
        reader.readAll();
    }

    /**
     * 创建数据收集监听器
     */
    private static <T> AnalysisEventListener<T> createDataCollectListener(List<T> dataList) {
        return new AnalysisEventListener<T>() {
            @Override
            public void invoke(T data, AnalysisContext context) {
                dataList.add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                log.debug("Excel导入完成，共导入{}条数据", dataList.size());
            }
        };
    }

    // ==================== 工具方法 ====================

    /**
     * 设置 Excel 响应头
     *
     * @param response HTTP响应对象
     * @param fileName 文件名
     * @throws IOException IO异常
     */
    public static void setExcelResponseHeader(HttpServletResponse response, String fileName) throws IOException {
        // 重置响应
        response.reset();

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");

        response.setContentType(EXCEL_CONTENT_TYPE);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        // 禁用缓存
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setDateHeader("Expires", 0);
    }

    /**
     * 获取 Excel 文件名（去除扩展名）
     *
     * @param fileName 原始文件名
     * @return 去除扩展名后的文件名
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    /**
     * 判断文件是否为 Excel 文件
     *
     * @param fileName 文件名
     * @return 是否为 Excel 文件
     */
    public static boolean isExcelFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".xlsx") || lowerFileName.endsWith(".xls");
    }
}
