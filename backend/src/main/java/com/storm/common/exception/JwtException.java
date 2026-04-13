// JwtException.java
package com.storm.common.exception;

import com.storm.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class JwtException extends RuntimeException {
    
    private final int code;
    
    public JwtException(String message) {
        super(message);
        this.code = ErrorCode.UNAUTHORIZED.getCode(); // 默认未授权状态
    }
    
    public JwtException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    
    public JwtException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public JwtException(String message, Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.UNAUTHORIZED.getCode();
    }
}