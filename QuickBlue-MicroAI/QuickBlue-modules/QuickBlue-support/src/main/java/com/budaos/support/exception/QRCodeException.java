package com.budaos.support.exception;

import lombok.Getter;

/**
 * 二维码异常
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
@Getter
public class QRCodeException extends RuntimeException {

    private final String errorCode;

    public QRCodeException(String message) {
        super(message);
        this.errorCode = "QR_CODE_ERROR";
    }

    public QRCodeException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "QR_CODE_ERROR";
    }

    public QRCodeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public QRCodeException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
