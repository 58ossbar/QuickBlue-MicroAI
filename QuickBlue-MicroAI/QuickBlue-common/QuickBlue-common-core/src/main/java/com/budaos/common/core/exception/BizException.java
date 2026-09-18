package com.budaos.common.core.exception;

import com.budaos.common.core.code.UserErrorCodes;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author budaos
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误信息
     */
    private String message;

    public BizException(String message) {
        super(message);
        this.code = String.valueOf(UserErrorCodes.PARAM_ERROR.getCode());
        this.message = message;
    }

    public BizException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
