package com.storm.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应结果")
public class Result<T> {
    @Schema(description = "业务状态码")
    private Integer code;
    @Schema(description = "提示信息")
    private String message;
    @Schema(description = "具体的返回数据")
    private T data;

    // 便捷方法：成功
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(200, message,null);
    }
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(200, message, data);
    }

    // 便捷方法：失败
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}