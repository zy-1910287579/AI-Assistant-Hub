package com.storm.controller.admin;


import com.storm.common.vo.Result;
import com.storm.context.UserContext;
import com.storm.entity.Order;
import com.storm.entity.TokenPayload;
import com.storm.entity.User;
import com.storm.service.OrderService;
import com.storm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author Storm
 * @since 2026-03-31
 */
@Tag(name = "管理端-用户数据管理",description = "管理员可修改账号信息")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/user")
@RestController
public class AminUserController {
    private final UserService userService;

    private final OrderService orderService;

    //controller里不做返回值判断,业务层已经主动抛出异常

    /**
     * 获取所有用户列表
     */
    @Operation(summary = "获取所有用户", description = "获取系统中所有用户信息")
    @GetMapping("userlist")
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.list();
        // 不返回密码等敏感信息
        users.forEach(user -> user.setPassword(null));
        return Result.success(users);
    }
    /**
     * 更新用户VIP等级
     */
    @Operation(summary = "更新用户VIP等级", description = "更新用户的VIP等级")
    @PutMapping("/{userId}/vip-level")
    public Result<Boolean> updateVipLevel(
            @PathVariable String userId,
            @RequestParam Integer vipLevel) {
        userService.updateVipLevel(userId, vipLevel);
        return Result.success(true, "用户VIP等级更新成功");
    }

    /**
     * 更新用户账号状态
     */
    @Operation(summary = "更新用户状态", description = "更新用户的启用/禁用状态")
    @PutMapping("/{userId}/status")
    public Result<Boolean> updateUserStatus(
            @PathVariable String userId,
            @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success(true, "用户状态更新成功");

    }


    @Operation(summary = "更新用户积分", description = "更新当前用户的积分，需要有效的JWT令牌")
    @PutMapping("/points")
    public Result<Boolean> updateUserPoints(
            @Parameter(description = "积分变化值，正数增加积分，负数减少积分", required = true)
            @RequestParam int pointsChange) {

        TokenPayload tokenPayload = UserContext.getUser();
        if (tokenPayload == null) {
            return Result.error("未认证的用户");
        }

        String currentUserId = tokenPayload.getUserId();
        log.info("更新用户积分: userId={}, pointsChange={}", currentUserId, pointsChange);
       userService.updateUserPoints(currentUserId, pointsChange);
       return Result.success(true,"积分更新成功") ;
    }

    @Operation(summary = "根据用户id查看所有订单")
    @GetMapping("/{userId}/orders")
    public Result<List<Order>> getUserOrders(@PathVariable String userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return Result.success(orders);
    }

}
