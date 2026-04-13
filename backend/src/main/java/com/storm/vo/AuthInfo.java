// AuthUser.java - 用于认证的用户信息
package com.storm.vo;

import lombok.Data;

@Data
public class AuthInfo {
    private String userId;
    private String username;
    private String role;
    private String displayName;
    private Integer status;
}