package com.budaos.common.excel.domain;

import java.util.List;

/**
 * Excel 导出数据传输对象
 * <p>
 * 封装导出 Excel 所需的所有参数，支持灵活配置导出行为
 * </p>
 *
 * @author QuickBlue
 * @since 4.0.0
 */
public class ExcelExportDTO {

    /**
     * 导出数据列表
     */
    private List<?> dataList;

    /**
     * Excel 文件名称（不带后缀）
     */
    private String fileName;

    /**
     * Sheet 名称
     */
    private String sheetName;

    /**
     * 导出数据的类类型
     */
    private Class<?> clazz;

    /**
     * 是否包含表头（默认包含）
     */
    private boolean includeHeader = true;

    /**
     * 是否自动调整列宽（默认自动调整）
     */
    private boolean autoWidth = true;

    public ExcelExportDTO() {
    }

    /**
     * 构造函数 - 基础配置
     *
     * @param dataList 数据列表
     * @param fileName 文件名
     * @param clazz    数据类型
     */
    public ExcelExportDTO(List<?> dataList, String fileName, Class<?> clazz) {
        this.dataList = dataList;
        this.fileName = fileName;
        this.clazz = clazz;
    }

    /**
     * 构造函数 - 完整配置
     *
     * @param dataList    数据列表
     * @param fileName    文件名
     * @param sheetName   Sheet名称
     * @param clazz       数据类型
     * @param includeHeader 是否包含表头
     * @param autoWidth   是否自动列宽
     */
    public ExcelExportDTO(List<?> dataList, String fileName, String sheetName,
                          Class<?> clazz, boolean includeHeader, boolean autoWidth) {
        this.dataList = dataList;
        this.fileName = fileName;
        this.sheetName = sheetName;
        this.clazz = clazz;
        this.includeHeader = includeHeader;
        this.autoWidth = autoWidth;
    }

    // Getters and Setters

    public List<?> getDataList() {
        return dataList;
    }

    public void setDataList(List<?> dataList) {
        this.dataList = dataList;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public boolean isIncludeHeader() {
        return includeHeader;
    }

    public void setIncludeHeader(boolean includeHeader) {
        this.includeHeader = includeHeader;
    }

    public boolean isAutoWidth() {
        return autoWidth;
    }

    public void setAutoWidth(boolean autoWidth) {
        this.autoWidth = autoWidth;
    }

    /**
     * 构建器模式
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 构建器
     */
    public static class Builder {
        private List<?> dataList;
        private String fileName;
        private String sheetName;
        private Class<?> clazz;
        private boolean includeHeader = true;
        private boolean autoWidth = true;

        public Builder dataList(List<?> dataList) {
            this.dataList = dataList;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder sheetName(String sheetName) {
            this.sheetName = sheetName;
            return this;
        }

        public Builder clazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder includeHeader(boolean includeHeader) {
            this.includeHeader = includeHeader;
            return this;
        }

        public Builder autoWidth(boolean autoWidth) {
            this.autoWidth = autoWidth;
            return this;
        }

        public ExcelExportDTO build() {
            return new ExcelExportDTO(dataList, fileName, sheetName, clazz, includeHeader, autoWidth);
        }
    }
}
