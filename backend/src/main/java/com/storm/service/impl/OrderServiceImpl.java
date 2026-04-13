package com.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.storm.common.vo.Result;
import com.storm.common.enums.ErrorCode;
import com.storm.common.exception.BusinessException;
import com.storm.entity.Order;
import com.storm.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Storm
 * @since 2026-03-31
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements com.storm.service.OrderService {


    @Override
    public List<Order> getOrdersByUserId(String userId) {
        List<Order> orders = this.baseMapper.selectList(new QueryWrapper<Order>()
                .eq("user_id", userId)
                .orderByDesc("created_at"));
        if(orders == null){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "订单查询失败");
        }
        return orders;
    }

    @Override
    @Transactional
    public Result<String> createOrder(Order order) {
        // 生成唯一订单ID
        String orderId = UUID.randomUUID().toString().replace("-", "");
        order.setOrderId(orderId)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());
        // 设置默认状态为待付款
        if (order.getStatus() == null) {
            order.setStatus(0);
        }
        boolean saved = save(order);
        if (saved) {
            log.info("订单创建成功: orderId={}, userId={}", orderId, order.getUserId());
            return Result.success(orderId, "订单创建成功");
        } else {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "订单创建失败");
        }

    }

    @Override
    public Order getOrderById(String orderId) {
        Order order = this.baseMapper.selectOne(new QueryWrapper<Order>()
                .eq("order_id", orderId));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        return order;
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(String orderId, Integer status) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }

        order.setStatus(status)
                .setUpdatedAt(LocalDateTime.now());

        return updateById(order);
    }

    @Override
    public List<Order> getOrdersByUserIdAndStatus(String userId, Integer status) {
        List<Order> orders = this.baseMapper.selectList(new QueryWrapper<Order>()
                .eq("user_id", userId)
                .eq("status", status)
                .orderByDesc("created_at"));
        if(orders == null){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "订单查询失败");
        }
        return orders;
    }

    @Override
    @Transactional
    public Result<Boolean> cancelOrder(String orderId, String userId) {
        
        Order order = getOne(new QueryWrapper<Order>()
                .eq("order_id", orderId)
                .eq("user_id", userId));

        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }

        // 检查订单状态是否允许取消（通常只有待付款状态可以取消）
        if (order.getStatus() != 0) { // 0: 待付款
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不允许取消");
        }

        order.setStatus(-1) // 假设-1表示已取消状态
                .setUpdatedAt(LocalDateTime.now());

        boolean updated = updateById(order);
        if (updated) {
            log.info("订单取消成功: orderId={}", orderId);
            return Result.success(true, "订单已取消");
        } else {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "订单取消失败");
        }
      
    }

    @Override
    @Transactional
    public Result<Boolean> deleteOrder(String orderId, String userId) {
        
        Order order = getOne(new QueryWrapper<Order>()
                .eq("order_id", orderId)
                .eq("user_id", userId));

        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }

        // 检查订单状态是否允许删除（通常已完成的订单才能删除）
        if (order.getStatus() != 4) { // 假设99表示已完成且可以删除的状态
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不允许删除");
        }

        boolean deleted = removeById(orderId);
        if (deleted) {
            log.info("订单删除成功: orderId={}", orderId);
            return Result.success(true, "订单已删除");
        } else {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "订单删除失败");
        }

    }


}
