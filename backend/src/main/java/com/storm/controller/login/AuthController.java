package com.storm.controller.login;
import com.storm.dto.LoginRequestDTO;
import com.storm.common.vo.Result;
import com.storm.service.AdminService;
import com.storm.service.UserService;
import com.storm.vo.LoginUserVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户和管理员认证登录")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AdminService adminService;
    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<LoginUserVO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {

        String role = loginRequest.getRole();
        // 根据角色分发请求
        // 假设数据库里存的是 "admin" 或 "super_admin"
        if ("admin".equals(role)) {
            // 调用管理员 Service
            return adminService.adminLogin(loginRequest);
        } else {
            // 调用普通用户 Service (默认)
            return userService.userLogin(loginRequest);
        }
    }
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功");
    }


}