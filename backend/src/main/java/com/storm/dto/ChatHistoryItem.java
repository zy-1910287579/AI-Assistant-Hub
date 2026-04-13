// package com.storm.dto 或你常用的 dto 包下
package com.storm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "问答回复组对象")
@Data
public class ChatHistoryItem {
    @Schema(description = "用户消息")
    private String userMessage;
    @Schema(description = "ai回复")
    private String aiMessage;
    @Schema(description = "可用用户提问的时间")
    private LocalDateTime timestamp; // 可用用户提问的时间
}