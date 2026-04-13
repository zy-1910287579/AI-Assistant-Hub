package com.storm.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举
 * 规范建议：
 * 1. 200: 成功
 * 2. 4xx: 客户端错误 (参数错误、权限问题)
 * 3. 5xx: 服务端错误 (系统异常)
 * 4. 1xxxx: 业务自定义错误 (AI相关、工具调用、RAG等)
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ================= 通用状态 =================
    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 请求失败，参数可能有问题
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * 未授权/未登录
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 禁止访问/权限不足
     */
    FORBIDDEN(403, "禁止访问，权限不足"),

    /**
     * 资源未找到
     */
    NOT_FOUND(404, "请求的资源不存在"),

    /**
     * 内部服务器错误
     */
    INTERNAL_ERROR(500, "系统内部错误，请联系管理员"),

    // ================= AI 与 RAG 业务错误 (10000 - 19999) =================

    /**
     * AI 服务通用错误
     */
    AI_SERVICE_ERROR(10001, "AI 服务响应异常"),

    /**
     * AI 连接超时
     */
    AI_CONNECTION_TIMEOUT(10002, "AI 服务连接超时，请重试"),

    /**
     * 提示词构建失败
     */
    AI_PROMPT_ERROR(10003, "AI 提示词构建失败"),

    /**
     * 工具调用失败
     */
    TOOL_CALLING_FAILED(10010, "工具调用执行失败"),

    /**
     * 知识库检索失败
     */
    RAG_RETRIEVAL_ERROR(10020, "知识库检索异常"),

    RAG_STORGE_ERROR(10099, "知识库向量化异常"),

    /**
     * 文档解析失败 (Tika相关)
     */
    DOCUMENT_PARSE_ERROR(10021, "文档解析失败，请检查文件格式"),

    // ================= 业务逻辑错误 (20000 - 29999) =================

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(20001, "用户不存在"),

    /**
     * 订单不存在
     */
    ORDER_NOT_FOUND(20002, "订单不存在"),

    /**
     * 会话不存在
     */
    SESSION_NOT_FOUND(20003, "会话已过期或不存在"),

    /**
     * 账户已被禁用
     */
    ACCOUNT_DISABLED(20004, "账户已被禁用，请联系客服"),

    /**
     * 用户名已被注册
     */
    USERNAME_EXISTS(20005, "用户名已被注册"),

    /**
     * 原密码输入错误
     */
    INVALID_PASSWORD(20006, "原密码输入错误"),

    /**
     * 订单状态不允许变更
     */
    ORDER_STATUS_NOT_ALLOWED(20007, "订单状态不允许变更"),

    /**
     * 订单状态不允许取消
     */
    ORDER_CANCEL_NOT_ALLOWED(20008, "订单状态不允许取消"),

    /**
     * 订单状态不允许删除
     */
    ORDER_DELETE_NOT_ALLOWED(20009, "订单状态不允许删除"),

    /**
     * 工单不存在
     */
    TICKET_NOT_FOUND(20010, "工单不存在"),

    /**
     * 管理员不存在
     */
    ADMIN_NOT_FOUND(20011, "管理员不存在"),

    /**
     * 工单已进入处理流程
     */
    TICKET_IN_PROCESS(20012, "工单已进入处理流程，无法修改"),

    /**
     * 工单不允许取消
     */
    TICKET_CANCEL_NOT_ALLOWED(20013, "工单已进入处理流程，无法取消"),

    /**
     * 管理员账号已存在
     */
    ADMIN_EXISTS(20014, "管理员账号已存在"),

    /**
     * 文档不存在
     */
    DOCUMENT_NOT_FOUND(20015, "文档不存在"),

    /**
     * 同名文件已存在
     */
    FILE_ALREADY_EXISTS(20016, "同名文件已存在"),

    /**
     * 文件不存在
     */
    FILE_NOT_FOUND(20017, "文件不存在"),

    /**
     * 文件超出大小限制
     */
    FILE_SIZE_EXCEEDED(20018, "文件大小超出限制"),



    INTERNAL_SERVER_ERROR(20019,"工单流程存在问题" );

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;
}
