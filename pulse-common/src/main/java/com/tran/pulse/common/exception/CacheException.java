package com.tran.pulse.common.exception;

/**
 * cache异常
 * @author tran
 * @version 1.0.0.0
 * @date 2025/6/25 11:02
 **/
public class CacheException extends PulseException {


    public CacheException(String code, String message) {
        super(code, message);
    }

    public CacheException(String message) {
        super(message);
    }


}
