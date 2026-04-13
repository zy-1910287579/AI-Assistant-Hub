package com.storm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClearSessionRequest {
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
}