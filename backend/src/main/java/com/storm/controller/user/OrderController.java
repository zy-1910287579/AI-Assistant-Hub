package com.storm.controller.user;


import com.storm.common.vo.Result;
import com.storm.context.UserContext;
import com.storm.entity.Order;
import com.storm.entity.TokenPayload;
import com.storm.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Storm
 * @since 2026-03-31
 */
@Tag(name = "用户端-订单管理")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/order")
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     *
     * @param order 订单信息
     * @return 创建结果
     */
    @Operation(summary = "创建订单", description = "创建一个新的订单，需要有效的JWT令牌")
    @PostMapping
    public Result<String> createOrder(@Valid @RequestBody Order order) {
        TokenPayload tokenPayload = UserContext.getUser();
        if (tokenPayload == null) {
            return Result.error("未认证的用户");
        }

        // 设置当前用户ID
        order.setUserId(tokenPayload.getUserId());

        log.info("创建订单: userId={}, productName={}", tokenPayload.getUserId(), order.getProductName());
        return orderService.createOrder(order);
    }

    /**
     * 获取当前用户的所有订单
     *
     * @return 订单列表
     */
    @Operation(summary = "获取当前用户的所有订单", description = "获取当前认证用户的所有订单")
    @GetMapping
    public Result<List<Order>> getAllOrders() {
        TokenPayload tokenPayload = UserContext.getUser();
        if (tokenPayload == null) {
            return Result.error("未认证的用户");
        }

        log.info("获取用户订单列表: userId={}", tokenPayload.getUserId());
        List<Order> orders = orderService.getOrdersByUserId(tokenPayload.getUserId());
        return Result.success(orders);
    }

    /**
     * 根据订单ID获取订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    @GetMapping("/{orderId}")
    public Result<Order> getOrderDetail(
            @Parameter(description = "订单ID", required = true)
            @PathVariable String orderId) {
        TokenPayload tokenPayload = UserContext.getUser();
        if (tokenPayload == null) {
            return Result.error("未认证的用户");
        }

        Order order = orderService.getOrderById(orderId);
        // 验证订单属于当前用户
        if (!order.getUserId().equals(tokenPayload.getUserId())) {
            return Result.error("无权限访问此订单");
        }

        log.info("获取订单详情: orderId={}", orderId);
        return Result.success(order);
    }

    /**
     * 根据订单状态获取订单列表
     *
     * @param status 订单状态
     * @return 订单列表
     */
    @Operation(summary = "根据状态获取订单列表", description = "根据订单状态获取当前用户的订单列表")
    @GetMapping("/status/{status}")
    public Result<List<Order>> getOrdersByStatus(
            @Parameter(description = "订单状态", required = true)
            @PathVariable Integer status) {
        TokenPayload tokenPayload = UserContext.getUser();
        if (tokenPayload == null) {
            return Result.error("未认证的用户");
        }

        log.info("根据状态获取订单列表: userId={}, status={}", tokenPayload.getUserId(), status);
        List<Order> orders = orderService.getOrdersByUserIdAndStatus(tokenPayload.getUserId(), status);
        return Result.success(orders);
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 取消结果
     */
    @Operation(summary = "取消订单", description = "取消当前用户的指定订单")
    @PutMapping("/{orderId}/cancel")
    public Result<Boolean> cancelOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable String orderId) {
        TokenPayload tokenPayload = UserContext.getUser();
        if (tokenPayload == null) {
            return Result.error("未认证的用户");
        }

        log.info("取消订单: orderId={}, userId={}", orderId, tokenPayload.getUserId());
        return orderService.cancelOrder(orderId, tokenPayload.getUserId());
    }

    /**
     * 删除订单
     *
     * @param orderId 订单ID
     * @return 删除结果
     */
    @Operation(summary = "删除订单", description = "删除当前用户的指定订单")
    @DeleteMapping("/{orderId}")
    public Result<Boolean> deleteOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable String orderId) {
        TokenPayload tokenPayload = UserContext.getUser();
        if (tokenPayload == null) {
            return Result.error("未认证的用户");
        }

        log.info("删除订单: orderId={}, userId={}", orderId, tokenPayload.getUserId());
        return orderService.deleteOrder(orderId, tokenPayload.getUserId());
    }





}
