package com.storm.common.exception;

import com.storm.common.enums.ErrorCode;
import lombok.Getter;


/**
 * 业务异常类
 * 用于在业务逻辑层（Service）抛出，
 * 被全局异常处理器捕获后，返回给前端友好的提示信息，而不是 500 堆栈。
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 构造方法 1：传入自定义错误码和消息
     * 场景：不想定义枚举，临时抛出一个特定错误
     */
    public BusinessException(Integer code, String message) {
        super(message); // 调用父类 RuntimeException 的构造器，保存消息
        this.code = code;
    }

    /**
     * 构造方法 2：传入错误码枚举（推荐）
     * 场景：使用定义好的 ErrorCode，保证规范统一
     * 示例：throw new BusinessException(ErrorCode.USER_NOT_FOUND);
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 将枚举中的消息传递给父类
        this.code = errorCode.getCode();
    }

    /**
     * 构造方法 3：传入错误码枚举和自定义详细消息
     * 场景：使用枚举的 Code，但需要动态拼接更详细的错误原因
     * 示例：throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI服务连接超时：OpenAI API 502");
     */
    public BusinessException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.code = errorCode.getCode();
    }

}
