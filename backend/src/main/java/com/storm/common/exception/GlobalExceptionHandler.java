package com.storm.common.exception;

import com.storm.common.vo.Result;
import com.storm.common.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 业务异常处理
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("⚠️ 业务异常：[{}] - {}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // parma参数绑定校验异常处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");

        log.warn("⚠️ 参数校验失败：{}", message);
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    // 集合/参数校验异常处理
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("参数格式不正确");

        log.warn("⚠️ 集合/参数校验失败：{}", message);
        return Result.error(400, message);
    }

    // 参数绑定异常处理
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleBindException(BindException e) {
        String message = e.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("请求参数格式错误");

        log.warn("⚠️ 参数绑定失败：{}", message);
        return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    // JWT 认证异常处理
    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleJwtException(JwtException e) {
        log.warn("⚠️ JWT 认证异常：[{}] - {}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 系统未知异常处理
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGlobalException(Exception e) {
        log.error("❌ 系统未知异常：", e);
        return Result.error(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }

}