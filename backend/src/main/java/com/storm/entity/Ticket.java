package com.storm.entity;

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
@TableName("t_ticket")

public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工单ID (主键, 自增)
     */
    @TableId(value = "ticket_id", type = IdType.AUTO)
    private Integer ticketId;

    /**
     * 提交工单的用户ID (外键，关联 t_user 表)
     */
    @TableField("user_id")
    private String userId;

    /**
     * 关联的订单ID (外键，关联 t_order 表，可为空)
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 工单类别 (如: 物流问题, 产品质量, 售后服务)
     */
    @TableField("category")
    private String category;

    /**
     * 工单标题
     */
    @TableField("title")
    private String title;

    /**
     * 工单详细描述
     */
    @TableField("description")
    private String description;

    /**
     * 工单优先级 (1: 低, 2: 中, 3: 高)
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 工单状态 (0: 待处理, 1: 处理中, 2: 已解决, 3: 已关闭)
     */
    @TableField("status")
    private Integer status;

    /**
     * 被分配的处理人 (员工ID或姓名)
     */
    @TableField("assigned_to")
    private String assignedTo;

    /**
     * 问题解决时间
     * 注意：由于数据库为 TIMESTAMPTZ 类型，此处暂时使用 Object 类型以绕开时区转换问题。
     * 待后续配置解决后，可改为 LocalDateTime。
     */
    @TableField("resolved_at")
    private Object resolvedAt;

    /**
     * 工单关闭时间
     * 注意：由于数据库为 TIMESTAMPTZ 类型，此处暂时使用 Object 类型以绕开时区转换问题。
     * 待后续配置解决后，可改为 LocalDateTime。
     */
    @TableField("closed_at")
    private Object closedAt;

    /**
     * 工单创建时间
     * 注意：由于数据库为 TIMESTAMPTZ 类型，此处暂时使用 Object 类型以绕开时区转换问题。
     * 待后续配置解决后，可改为 LocalDateTime。
     */
    @TableField("created_at")
    private Object createdAt;

    /**
     * 工单信息更新时间
     * 注意：由于数据库为 TIMESTAMPTZ 类型，此处暂时使用 Object 类型以绕开时区转换问题。
     * 待后续配置解决后，可改为 LocalDateTime。
     */
    @TableField("updated_at")
    private Object updatedAt;


}
