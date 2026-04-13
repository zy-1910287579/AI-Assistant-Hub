package com.storm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin") // 对应数据库表名
public class Admin {
    @TableId(type = IdType.ASSIGN_ID)
    private String userId;
    private String username;
    private String phoneNumber;
    private String email;
    private String password;
    private String role; // 比如 'admin' 或 'super_admin'
    private Integer status; // 1: 正常, 0: 禁用
    private Object createdAt;
    private Object updatedAt;
}