package com.storm.controller.user;


import com.storm.common.vo.Result;
import com.storm.context.UserContext;
import com.storm.entity.TokenPayload;
import com.storm.entity.User;
import com.storm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author Storm
 * @since 2026-03-31
 */
@Tag(name = "用户端-用户数据管理",description = "用户可实现修改账号信息")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user/customer")
@RestController
public class UserController {
    private final UserService userService;
    @Operation(summary = "用户注册", description = "新用户注册接口")
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody User user) {
        log.info("用户注册: {}", user.getUsername());
        return userService.register(user);
    }
    @Operation(summary = "更新用户密码", description = "更新当前用户的密码，需要有效的JWT令牌")
    @PutMapping("/password")
    public Result<Boolean> updatePassword(
            @Parameter(description = "旧密码", required = true)
            @RequestParam String oldPassword,
            @Parameter(description = "新密码", required = true)
            @RequestParam String newPassword) {

        TokenPayload tokenPayload = UserContext.getUser();
        if (tokenPayload == null) {
            return Result.error("未认证的用户");
        }

        String currentUserId = tokenPayload.getUserId();
        log.info("更新用户密码: userId={}", currentUserId);

        userService.updatePassword(currentUserId, oldPassword, newPassword);
        return Result.success(true, "密码更新成功");
    }



    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前认证用户的信息")
    @GetMapping("/userInfo")
    public Result<User> getCurrentUserProfile() {
        TokenPayload tokenPayload = UserContext.getUser();
        if (tokenPayload == null) {
            return Result.error("未认证的用户");
        }

        String currentUserId = tokenPayload.getUserId();
        User user = userService.getById(currentUserId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 不返回密码等敏感信息
        user.setPassword(null);
        return Result.success(user);
    }

}
