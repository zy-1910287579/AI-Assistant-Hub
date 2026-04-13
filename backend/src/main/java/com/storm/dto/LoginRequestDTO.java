// LoginRequest.java
package com.storm.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import javax.validation.constraints.Size;
@Data
@Schema(description = "登录请求参数")
public class LoginRequestDTO {

    @Schema(description = "账号", required = true)
    @NotBlank(message = "账号不能为空")
    @Size(min = 1, max = 50, message = "账号长度必须在1-50个字符之间")
    private String account;

    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @Schema(description = "角色", required = true)
    @NotBlank(message = "角色不能为空")
    @Size(min = 1, max = 20, message = "角色长度必须在1-20个字符之间")
    private String role;
}