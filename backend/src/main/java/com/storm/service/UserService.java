package com.storm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.storm.common.vo.Result;
import com.storm.dto.LoginRequestDTO;
import com.storm.entity.User;
import com.storm.vo.LoginUserVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Storm
 * @since 2026-03-31
 */
public interface UserService extends IService<User> {

    boolean updateUserPoints(String userId, int pointsChange);

    /**
     * 用户登录
     */
    Result<LoginUserVO> userLogin(LoginRequestDTO loginRequestDTO);

    /**
     * 刷新令牌
     */
    Result<LoginUserVO> refreshToken(String refreshToken);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据用户名和密码查找用户
     */
    User findByUsernameAndPassword(String username, String password);

    /**
     * 注册新用户
     */
    Result<String> register(User user);

    /**
     * 更新用户密码
     */
    boolean updatePassword(String userId, String oldPassword, String newPassword);


    /**
     * 更新用户账号状态
     * @param userId 用户ID
     * @param status 账号状态
     * @return 是否更新成功
     */
    boolean updateUserStatus(String userId, Integer status);

    /**
     * 更新用户VIP等级
     * @param userId 用户ID
     * @param vipLevel VIP等级
     * @return 是否更新成功
     */
    boolean updateVipLevel(String userId, Integer vipLevel);

}
