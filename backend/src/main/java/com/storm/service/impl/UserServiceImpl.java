package com.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.storm.common.vo.Result;
import com.storm.common.enums.ErrorCode;
import com.storm.common.exception.BusinessException;
import com.storm.common.exception.JwtException;
import com.storm.common.utils.JwtUtil;
import com.storm.dto.LoginRequestDTO;
import com.storm.vo.AuthInfo;
import com.storm.entity.User;
import com.storm.mapper.UserMapper;
import com.storm.vo.LoginUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/*
1.用户登录
2.更新用户积分
3.新用户注册
4.更新密码
*/
@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements com.storm.service.UserService {

    private final JwtUtil jwtUtil;
    //这里的“查询-计算-更新”过程，在高并发场景下，会产生一个经典的“竞态条件”问题。
    //这个问题需要通过事务隔离和乐观锁或悲观锁来解决。这已经超出了 MP 通用方法的能力范围，是需要在 Service 层精心设计的业务逻辑。
    // ... 其他方法 ...
    @Override
    @Transactional // 重要：确保操作的原子性
    public boolean updateUserPoints(String userId, int pointsChange) {
        User existingUser = this.getById(userId);
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (pointsChange == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "积分变更值不能为 0");
        }

        long newPoints = Math.max(0, existingUser.getPointsBalance() + pointsChange);
        existingUser.setPointsBalance(newPoints);

        boolean updated = this.updateById(existingUser);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "积分更新失败");
        }
        return true;
    }

    //用户登录,前端展示的用户信息就是这里的返回体里的
    @Override
    public Result<LoginUserVO> userLogin(LoginRequestDTO loginRequestDTO) {
        // 查询用户
        User user = findByUsernameAndPassword(
                loginRequestDTO.getAccount(),
                loginRequestDTO.getPassword()
        );


        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 转换为认证用户对象
        AuthInfo authInfo = convertToAuthUser(user);

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

    //刷新用户令牌,现阶段不实现
    @Override
    public Result<LoginUserVO> refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new JwtException("Token 已失效");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);

        User user = findByUsername(username);
        if (user == null || (user.getStatus() != null && user.getStatus() == 0)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在或已被禁用");
        }

        AuthInfo authInfo = convertToAuthUser(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", authInfo.getUserId());
        claims.put("username", authInfo.getUsername());
        claims.put("role", authInfo.getRole());

        String newAccessToken = jwtUtil.generateAccessToken(username, claims);

        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setAccessToken(newAccessToken);
        loginUserVO.setRefreshToken(refreshToken);

        return Result.success(loginUserVO, "令牌刷新成功");
    }

    //根据用户名查询一条信息,用于验证密码是否正确,在reflashToken里用
    @Override
    public User findByUsername(String username) {
        return getOne(new QueryWrapper<User>().eq("username", username), false);
    }
    //根据用户名和密码查询一条信息,用于验证密码是否正确
    @Override
    public User findByUsernameAndPassword(String username, String password) {
        if (username == null || password == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名或密码不能为空");
        }
        return getOne(new QueryWrapper<User>()
                .eq("username", username)
                .eq("password",DigestUtils.md5DigestAsHex(password.getBytes())), false);
    }


    //新用户注册
    @Override
    public Result<String> register(User user) {
        // 检查用户名是否已存在
        User existingUser = findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "用户名已存在");
        }


        // 设置默认值
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setRole(user.getRole() != null ? user.getRole() : "user"); // 默认为客户角色
        user.setStatus(user.getStatus() != null ? user.getStatus() : 1); // 默认启用
        user.setVipLevel(user.getVipLevel() != null ? user.getVipLevel() : 0); // 默认普通用户
        user.setPointsBalance(user.getPointsBalance() != null ? user.getPointsBalance() : 0L); // 默认积分

        // 保存用户,这里用的是mybatis-plus的save方法,就可以自动插入了
        boolean saved = save(user);
        if (saved) {
            return Result.success("注册成功", user.getUserId());
        } else {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "注册失败");
        }
    }

    @Override
    public boolean updateUserStatus(String userId, Integer status) {
        User user = getById(userId);
        if (user == null) {
            return false;
        }

        // 验证状态值的有效性（假设0=禁用，1=启用）
        if (status != 0 && status != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的状态值");
        }
        

        user.setStatus(status)
                .setUpdatedAt(LocalDateTime.now());

        boolean updated = updateById(user);
        if (updated) {
            log.info("更新用户状态成功: userId={}, status={}", userId, status);
        }
        return updated;
    }

    @Override
    public boolean updateVipLevel(String userId, Integer vipLevel) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 验证VIP等级的有效性（假设0-10级）
        if (vipLevel < 0 || vipLevel > 10) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的VIP等级");
        }

        user.setVipLevel(vipLevel)
                .setUpdatedAt(LocalDateTime.now());

        boolean updated = updateById(user);
        if (updated) {
            log.info("更新用户VIP等级成功: userId={}, vipLevel={}", userId, vipLevel);
        }
        return updated;
    }


    //更新密码,controller层会从tokens里获取这个userid,传给service,
    // 前端只需要关心发送新旧密码和自己的认证凭证（Token），而不需要知道也不应该知道自己的 userId。userId 的获取和传递是在后端的 Controller 层完成的，保证了安全性。
    @Override
    public boolean updatePassword(String userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "新密码不能为空");
        }
        // 验证旧密码
        String encryptedOldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        if (!user.getPassword().equals(encryptedOldPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码错误");
        }

        // 更新密码
        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes()));
        updateUser.setUpdatedAt(LocalDateTime.now());

        return updateById(updateUser);
    }

    //辅助方法
    /**
     * 将User实体转换为AuthUser认证对象
     */
    private AuthInfo convertToAuthUser(User user) {
        AuthInfo authInfo = new AuthInfo();
        authInfo.setUserId(user.getUserId());
        authInfo.setUsername(user.getUsername());
        authInfo.setRole(user.getRole());
        authInfo.setDisplayName(user.getUsername());
        authInfo.setStatus(user.getStatus());
        return authInfo;
    }






}
