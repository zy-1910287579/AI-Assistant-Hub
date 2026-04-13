package com.storm.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.storm.common.vo.Result;
import com.storm.common.enums.ErrorCode;
import com.storm.common.exception.BusinessException;
import com.storm.common.utils.JwtUtil;
import com.storm.dto.LoginRequestDTO;
import com.storm.entity.Admin;
import com.storm.mapper.AdminMapper;
import com.storm.service.AdminService;
import com.storm.vo.AuthInfo;
import com.storm.vo.LoginUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    private final JwtUtil jwtUtil;
    @Override
    public Result<LoginUserVO> adminLogin(LoginRequestDTO loginRequestDTO) {
        // 查询用户
        Admin admin = findByUsernameAndPassword(
                loginRequestDTO.getAccount(),
                loginRequestDTO.getPassword()
        );


        if (admin == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "账号或密码错误");
        }

        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "账户已被禁用");
        }
        // 转换为认证用户对象
        AuthInfo authInfo = convertToAuthUser(admin);

        // 生成令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", authInfo.getUserId());
        claims.put("username", authInfo.getUsername());
        claims.put("role", authInfo.getRole());

        String accessToken = jwtUtil.generateAccessToken(authInfo.getUsername(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(authInfo.getUsername(), claims);

        // 构建响应
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setAccessToken(accessToken);
        loginUserVO.setRefreshToken(refreshToken);
        loginUserVO.setAuthInfo(authInfo);

        return Result.success(loginUserVO,"登录成功");
    }

    @Override
    public Result<LoginUserVO> refreshToken(String refreshToken) {
        
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的刷新令牌");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 从数据库重新获取用户信息确保是最新的
        Admin admin = findByUsername(username);
        if (admin == null || (admin.getStatus() != null && admin.getStatus() == 0)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户不存在或已被禁用");
        }

        AuthInfo authInfo = convertToAuthUser(admin);

        // 创建新的访问令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", authInfo.getUserId());
        claims.put("username", authInfo.getUsername());
        claims.put("role", authInfo.getRole());

        String newAccessToken = jwtUtil.generateAccessToken(username, claims);

        // 构建响应
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setAccessToken(newAccessToken);
        loginUserVO.setRefreshToken(refreshToken);

        return Result.success(loginUserVO,"令牌刷新成功");
        
    }

    @Override
    public Admin findByUsername(String username) {
        return getOne(new QueryWrapper<Admin>().eq("username", username), false);
    }

    @Override
    public Admin findByUsernameAndPassword(String username, String password) {
        return getOne(new QueryWrapper<Admin>()
                .eq("username", username)
                .eq("password",DigestUtils.md5DigestAsHex(password.getBytes())), false);
    }

    //注册用户
    @Override
    public Result<String> register(Admin admin) {
        // 检查用户名是否已存在
        Admin existingUAdmin = findByUsername(admin.getUsername());
        if (existingUAdmin != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "用户名已存在");
        }

        // 设置默认值
        admin.setPassword(DigestUtils.md5DigestAsHex(admin.getPassword().getBytes()));
        admin.setRole(admin.getRole() != null ? admin.getRole() : "user"); // 默认为客户角色
        admin.setStatus(admin.getStatus() != null ? admin.getStatus() : 1); // 默认启用

        // 保存用户
        boolean saved = save(admin);
        if (saved) {
            return Result.success("注册成功", admin.getUserId());
        } else {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "注册失败");
        }
    }

    @Override
    public boolean updatePassword(String userId, String oldPassword, String newPassword) {
        Admin admin = getById(userId);
        if (admin == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 验证旧密码
        String encryptedOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!admin.getPassword().equals(encryptedOldPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码错误");
        }

        // 更新密码
        Admin updateAdmin = new Admin();
        updateAdmin.setUserId(userId);
        updateAdmin.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        updateAdmin.setUpdatedAt(LocalDateTime.now());

        return updateById(updateAdmin);
    }

    /**
     * 将User实体转换为AuthUser认证对象
     */
    private AuthInfo convertToAuthUser(Admin admin) {
        AuthInfo authInfo = new AuthInfo();
        authInfo.setUserId(admin.getUserId());
        authInfo.setUsername(admin.getUsername());
        authInfo.setRole(admin.getRole());
        authInfo.setDisplayName(admin.getUsername());
        authInfo.setStatus(admin.getStatus());
        return authInfo;
    }
}