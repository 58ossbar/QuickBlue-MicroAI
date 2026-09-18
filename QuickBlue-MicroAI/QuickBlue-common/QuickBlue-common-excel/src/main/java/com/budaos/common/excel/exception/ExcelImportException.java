package com.budaos.common.excel.exception;

/**
 * Excel 导入异常
 * <p>
 * 统一处理 Excel 导入过程中的异常
 * </p>
 *
 * @author QuickBlue
 * @since 4.0.0
 */
public class ExcelImportException extends RuntimeException {

    public ExcelImportException() {
        super();
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     */
    public ExcelImportException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message 异常消息
     * @param cause   原因异常
     */
    public ExcelImportException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     *
     * @param cause 原因异常
     */
    public ExcelImportException(Throwable cause) {
        super(cause);
    }
}
