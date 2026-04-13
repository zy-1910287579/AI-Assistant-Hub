package com.storm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.storm.common.vo.Result;
import com.storm.entity.Order;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Storm
 * @since 2026-03-31
 */
public interface OrderService extends IService<Order> {


    /**
     * 根据用户ID获取订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> getOrdersByUserId(String userId);

    /**
     * 创建订单
     * @param order 订单信息
     * @return 创建结果
     */
    Result<String> createOrder(Order order);

    /**
     * 根据订单ID获取订单详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    Order getOrderById(String orderId);

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 新状态
     * @return 更新结果
     */
    boolean updateOrderStatus(String orderId, Integer status);

    /**
     * 根据用户ID和订单状态获取订单列表
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> getOrdersByUserIdAndStatus(String userId, Integer status);

    /**
     * 取消订单
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 取消结果
     */
    Result<Boolean> cancelOrder(String orderId, String userId);

    /**
     * 删除订单
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 删除结果
     */
    Result<Boolean> deleteOrder(String orderId, String userId);




}
