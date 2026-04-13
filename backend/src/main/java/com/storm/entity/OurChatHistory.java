package com.storm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话历史实体类，映射 spring_ai_chat_memory 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OurChatHistory {

    /**
     * 主键 ID，自增
     */
    private Long id;

    /**
     * 对话会话ID，由 userId 和 sessionId 拼接而成
     */
    private String conversationId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型 (USER, ASSISTANT, SYSTEM, TOOL)
     */
    private String type;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
}