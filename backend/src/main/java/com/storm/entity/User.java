// 修改后的User.java
package com.storm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author Storm
 * @since 2026-03-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID (主键)
     */
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private String userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码 - 新增字段，用于登录验证
     */
    @TableField("password")
    private String password;

    /**
     * 角色 - 新增字段，区分admin/customer
     */
    @TableField("role")
    private String role;

    /**
     * 手机号码
     */
    @TableField("phone_number")
    private String phoneNumber;

    /**
     * 电子邮箱
     */
    @TableField("email")
    private String email;

    /**
     * VIP等级 (0: 普通用户, 1: 青铜, 2: 白银, 3: 黄金 ...)
     */
    @TableField("vip_level")
    private Integer vipLevel;

    /**
     * 积分余额
     */
    @TableField("points_balance")
    private Long pointsBalance;

    /**
     * 用户状态 (0: 禁用, 1: 启用)
     */
    @TableField("status")
    private Integer status;

    /**
     * 用户创建时间
     */
    @TableField("created_at")
    private Object createdAt;

    /**
     * 用户信息更新时间
     */
    @TableField("updated_at")
    private Object updatedAt;
}