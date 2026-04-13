package com.storm.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Storm
 * @since 2026-03-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_order")

public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID (主键)
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private String orderId;

    /**
     * 用户ID (外键，关联 t_user 表)
     */
    @TableField("user_id")
    private String userId;

    /**
     * 商品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 商品数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 订单总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 订单状态 (-1: 已取消, 0: 待付款, 1: 待发货, 2: 已发货, 3: 已签收, 4: 交易成功)
     */
    @TableField("status")
    private Integer status;

    /**
     * 物流公司名称
     */
    @TableField("logistics_company")
    private String logisticsCompany;

    /**
     * 物流单号
     */
    @TableField("tracking_number")
    private String trackingNumber;

    /**
     * 物流信息
     */
    @TableField("logistics_info")
    private String logisticsInfo;

    /**
     * 发货时间
     * 注意：由于数据库为 TIMESTAMPTZ 类型，此处暂时使用 Object 类型以绕开时区转换问题。
     * 待后续配置解决后，可改为 LocalDateTime。
     */
    @TableField("shipped_at")
    private Object shippedAt;

    /**
     * 签收时间
     * 注意：由于数据库为 TIMESTAMPTZ 类型，此处暂时使用 Object 类型以绕开时区转换问题。
     * 待后续配置解决后，可改为 LocalDateTime。
     */
    @TableField("delivered_at")
    private Object deliveredAt;

    /**
     * 订单创建时间
     * 注意：由于数据库为 TIMESTAMPTZ 类型，此处暂时使用 Object 类型以绕开时区转换问题。
     * 待后续配置解决后，可改为 LocalDateTime。
     */
    @TableField("created_at")
    private Object createdAt;

    /**
     * 订单更新时间
     * 注意：由于数据库为 TIMESTAMPTZ 类型，此处暂时使用 Object 类型以绕开时区转换问题。
     * 待后续配置解决后，可改为 LocalDateTime。
     */
    @TableField("updated_at")
    private Object updatedAt;

}
