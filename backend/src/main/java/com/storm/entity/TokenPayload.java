package com.storm.entity;

import lombok.Builder;
import lombok.Data;

//data除了构造函数都有
@Data
@Builder
public class TokenPayload {
    private String userId;
    private String username;
    private String role;
    private Long expireTime;
    // 可根据 JWT 中的字段扩展
}