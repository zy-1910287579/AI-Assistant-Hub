package com.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.storm.common.enums.ErrorCode;
import com.storm.common.exception.BusinessException;
import com.storm.entity.Ticket;
import com.storm.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Storm
 * @since 2026-03-31
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements com.storm.service.TicketService {

    @Override
    @Transactional // 重要：确保操作的原子性，如果失败则回滚
    public boolean createSupportTicket(Ticket ticket) {
        // 这里可以加入更多的业务逻辑
        // 例如：校验用户是否存在、校验订单ID是否属于该用户等
        // 调用 MP 的 save 方法保存工单
        boolean b = this.save(ticket);
        if(!b){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "工单创建失败");
        }
        return b;
    }
    @Override
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = this.baseMapper.selectList(new QueryWrapper<Ticket>().orderByDesc("created_at"));
        if(tickets == null){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "工单查询失败");
        }
        return tickets;
    }

    @Override
    public Ticket getTicketById(Integer ticketId) {
        Ticket ticket = getById(ticketId);
        if (ticket == null) {
            throw new BusinessException(ErrorCode.TICKET_NOT_FOUND, "工单不存在");
        }
        return ticket;
    }

    @Override
    public boolean updateTicketStatus(Integer ticketId, Integer status) {
        Ticket ticket = new Ticket();
        ticket.setTicketId(ticketId);
        ticket.setStatus(status);
        boolean b= updateById(ticket);
        if(!b){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "工单状态更新失败");
        }
        return b;
    }

    @Override
    public boolean assignTicket(Integer ticketId, String assignedTo) {
        Ticket ticket = new Ticket();
        ticket.setTicketId(ticketId);
        ticket.setAssignedTo(assignedTo);
        ticket.setUpdatedAt(LocalDateTime.now());
        boolean b = updateById(ticket);
        if(!b){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "工单分配失败");
        }
        return b;
    }

    @Override
    public List<Ticket> getTicketsByStatus(Integer status) {
        List<Ticket> tickets = this.baseMapper.selectList(new QueryWrapper<Ticket>()
                .eq("status", status)
                .orderByDesc("created_at"));
        if(tickets == null){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "工单查询失败");
        }
        return tickets;
    }

    @Override
    public List<Ticket> getTicketsByUserId(String userId) {
        List<Ticket> tickets = this.baseMapper.selectList(new QueryWrapper<Ticket>()
                .eq("user_id", userId)
                .orderByDesc("created_at"));
        if(tickets == null){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "工单查询失败");
        }
        return tickets;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserTicket(Integer ticketId, String userId, String category, String description) {
        // 1. 查询工单
        Ticket ticket = this.getById(ticketId);

        // 2. 校验工单是否存在，是否属于当前用户，以及状态是否为"待处理"(0)
        if (ticket == null || !ticket.getUserId().equals(userId) || ticket.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "工单不存在或状态错误");
        }
        
        // 3. 更新字段
        ticket.setCategory(category);
        ticket.setDescription(description);
        ticket.setUpdatedAt(LocalDateTime.now());

        // 4. 执行更新
        boolean b = this.updateById(ticket);
        if(!b){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "工单更新失败");
        }
        return b;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelUserTicket(Integer ticketId, String userId) {
        // 1. 查询工单
        Ticket ticket = this.getById(ticketId);

        // 2. 校验工单是否存在，是否属于当前用户，以及状态是否为"待处理"(0)
        if (ticket == null || !ticket.getUserId().equals(userId) || ticket.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "工单不存在或状态错误");
        }

        // 3. 更新状态为"已关闭"(3)
        ticket.setStatus(3);
        ticket.setClosedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        // 4. 执行更新
        boolean b = this.updateById(ticket);
        if(!b){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "工单取消失败");
        }
        return b;
    }


}
