package com.storm.service;

import com.storm.common.vo.Result;
import com.storm.dto.LoginRequestDTO;
import com.storm.entity.Admin;
import com.storm.vo.LoginUserVO;

public interface AdminService {


    /**
     * 用户登录
     */
    Result<LoginUserVO> adminLogin(LoginRequestDTO loginRequestDTO);

    /**
     * 刷新令牌
     */
    Result<LoginUserVO> refreshToken(String refreshToken);

    /**
     * 根据用户名查找用户
     */
    Admin findByUsername(String username);

    /**
     * 根据用户名和密码查找用户
     */
    Admin findByUsernameAndPassword(String username, String password);

    /**
     * 注册新用户
     */
    Result<String> register(Admin admin);

    /**
     * 更新用户密码
     */
    boolean updatePassword(String userId, String oldPassword, String newPassword);

}
