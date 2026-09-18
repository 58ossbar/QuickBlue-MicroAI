package com.budaos.common.core.domain;

import com.budaos.common.core.code.IErrorCode;
import com.budaos.common.core.code.SystemErrorCodes;
import com.budaos.common.core.code.UserErrorCodes;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import com.budaos.common.core.enums.ApiDataTypeEnum;

/**
 * 请求返回对象
 *
 */
@Data
@NoArgsConstructor
@Schema
public class ApiResult<T> {

    public static final int OK_CODE = 0;

    public static final String OK_MSG = "操作成功";

    @Schema(description = "返回码")
    private Integer code;

    @Schema(description = "级别")
    private String level;

    private String msg;

    private Boolean ok;

    @Schema(description = "返回数据")
    private T data;

    private ApiDataTypeEnum dataType;

    public ApiResult(Integer code, String level, boolean ok, String msg, T data) {
        this.code = code;
        this.level = level;
        this.ok = ok;
        this.msg = msg;
        this.data = data;
        this.dataType = ApiDataTypeEnum.NORMAL;
    }

    public ApiResult(Integer code, String level, boolean ok, String msg) {
        this.code = code;
        this.level = level;
        this.ok = ok;
        this.msg = msg;
        this.dataType = ApiDataTypeEnum.NORMAL;
    }

    public ApiResult(IErrorCode errorCode, boolean ok, String msg, T data) {
        this.code = errorCode.getCode();
        this.level = errorCode.getLevel();
        this.ok = ok;
        if (StringUtils.isNotBlank(msg)) {
            this.msg = msg;
        } else {
            this.msg = errorCode.getMsg();
        }
        this.data = data;
        this.dataType = ApiDataTypeEnum.NORMAL;
    }

    public ApiResult(IErrorCode errorCode, boolean ok, String msg) {
        this.code = errorCode.getCode();
        this.level = errorCode.getLevel();
        this.ok = ok;
        this.msg = msg;
        this.data = null;
        this.dataType = ApiDataTypeEnum.NORMAL;
    }

    public static <T> ApiResult<T> ok() {
        return new ApiResult<>(OK_CODE, null, true, OK_MSG, null);
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(OK_CODE, null, true, OK_MSG, data);
    }

    public static <T> ApiResult<T> okMsg(String msg) {
        return new ApiResult<>(OK_CODE, null, true, msg, null);
    }

    // -------------------------------------------- 最常用的 用户参数 错误码 --------------------------------------------

    public static <T> ApiResult<T> userErrorParam() {
        return new ApiResult<>(UserErrorCodes.PARAM_ERROR, false, null, null);
    }

    public static <T> ApiResult<T> userErrorParam(String msg) {
        return new ApiResult<>(UserErrorCodes.PARAM_ERROR, false, msg, null);
    }

    public static <T> ApiResult<T> paramError(String msg) {
        return userErrorParam(msg);
    }

    // -------------------------------------------- 错误码 --------------------------------------------

    public static <T> ApiResult<T> error(IErrorCode errorCode) {
        return new ApiResult<>(errorCode, false, null, null);
    }

    public static <T> ApiResult<T> error(IErrorCode errorCode, boolean ok) {
        return new ApiResult<>(errorCode, ok, null, null);
    }

    public static <T> ApiResult<T> error(ApiResult<?> responseDTO) {
        return new ApiResult<>(responseDTO.getCode(), responseDTO.getLevel(), responseDTO.getOk(), responseDTO.getMsg(), null);
    }

    public static <T> ApiResult<T> error(IErrorCode errorCode, String msg) {
        return new ApiResult<>(errorCode, false, msg, null);
    }

    public static <T> ApiResult<T> error(String code, String msg) {
        try {
            return new ApiResult<>(Integer.parseInt(code), IErrorCode.LEVEL_USER, false, msg, null);
        } catch (NumberFormatException e) {
            // 如果 code 不是数字，使用系统错误码
            return new ApiResult<>(SystemErrorCodes.SYSTEM_ERROR.getCode(), IErrorCode.LEVEL_SYSTEM, false, msg != null ? msg : SystemErrorCodes.SYSTEM_ERROR.getMsg(), null);
        }
    }

    public static <T> ApiResult<T> error(Integer code, String msg) {
        return new ApiResult<>(code, IErrorCode.LEVEL_USER, false, msg, null);
    }

    public static <T> ApiResult<T> errorData(IErrorCode errorCode, T data) {
        return new ApiResult<>(errorCode, false, null, data);
    }


}
