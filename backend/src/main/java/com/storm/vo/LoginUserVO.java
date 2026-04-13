// LoginUserVO.java - 登录用户视图对象
package com.storm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录用户信息")
public class LoginUserVO {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "用户信息")
    private AuthInfo authInfo;
}