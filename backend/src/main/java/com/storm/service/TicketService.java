package com.storm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.storm.entity.Ticket;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Storm
 * @since 2026-03-31
 */
public interface TicketService extends IService<Ticket> {

    boolean createSupportTicket(Ticket ticket);


    /**
     * 获取所有工单列表
     * @return 工单列表
     */
    List<Ticket> getAllTickets();

    /**
     * 根据工单ID获取工单详情
     * @param ticketId 工单ID
     * @return 工单详情
     */
    Ticket getTicketById(Integer ticketId);

    /**
     * 更新工单状态
     * @param ticketId 工单ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateTicketStatus(Integer ticketId, Integer status);

    /**
     * 分配工单给处理人
     * @param ticketId 工单ID
     * @param assignedTo 处理人
     * @return 是否分配成功
     */
    boolean assignTicket(Integer ticketId, String assignedTo);

    /**
     * 根据状态获取工单列表
     * @param status 状态
     * @return 工单列表
     */
    List<Ticket> getTicketsByStatus(Integer status);

    /**
     * 根据用户ID获取工单列表
     * @param userId 用户ID
     * @return 工单列表
     */
    List<Ticket> getTicketsByUserId(String userId);


    /**
     * 用户更新自己的工单（仅限待处理状态）
     * @param ticketId 工单ID
     * @param userId 用户ID
     * @param category 新的工单类别
     * @param description 新的工单描述
     * @return 是否更新成功
     */
    boolean updateUserTicket(Integer ticketId, String userId, String category, String description);



    /**
     * 用户取消自己的待处理工单
     * @param ticketId 工单ID
     * @param userId 用户ID
     * @return 是否取消成功
     */
    boolean cancelUserTicket(Integer ticketId, String userId);

}
