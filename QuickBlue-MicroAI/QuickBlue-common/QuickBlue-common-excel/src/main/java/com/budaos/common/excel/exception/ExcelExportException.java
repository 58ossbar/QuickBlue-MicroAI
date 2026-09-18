package com.budaos.common.excel.exception;

/**
 * Excel 导出异常
 * <p>
 * 统一处理 Excel 导出过程中的异常
 * </p>
 *
 * @author QuickBlue
 * @since 4.0.0
 */
public class ExcelExportException extends RuntimeException {

    public ExcelExportException() {
        super();
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public ExcelExportException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param cause   原因异常
     */
    public ExcelExportException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     *
     * @param cause 原因异常
     */
    public ExcelExportException(Throwable cause) {
        super(cause);
    }
}
