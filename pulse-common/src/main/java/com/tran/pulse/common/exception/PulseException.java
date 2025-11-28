package com.tran.pulse.common.exception;

import com.tran.pulse.common.constants.PulseHttpStatus;

/**
 * 自定义异常
 *
 * @version 1.0.0.0
 * @date 2025/3/26 14:34
 * @author tran
 **/
public class PulseException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    /**
     * 异常状态码
     */
    private String code;

    /**
     * 异常信息
     */
    private String message;


    private PulseException() {
    }

    public PulseException(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public PulseException(Integer code, String message) {
        this.message = message;
        this.code = String.valueOf(code);
    }


    public PulseException(String message) {
        this.message = message;
        this.code = String.valueOf(PulseHttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

}
