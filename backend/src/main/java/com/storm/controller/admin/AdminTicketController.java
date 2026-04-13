package com.storm.controller.admin;


import com.storm.common.vo.Result;
import com.storm.entity.Ticket;
import com.storm.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理端-工单管理", description = "管理端工单管理相关API接口")
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/admin/ticket")
@RestController
public class AdminTicketController {

    private final TicketService ticketService;

    /**
     * 获取所有工单列表
     */
    @Operation(summary = "获取所有工单", description = "获取系统中所有工单信息")
    @GetMapping
    public Result<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        return Result.success(tickets);
    }

    /**
     * 根据工单ID获取工单详情
     */
    @Operation(summary = "获取工单详情", description = "根据工单ID获取工单详细信息")
    @GetMapping("/{ticketId}")
    public Result<Ticket> getTicketById(@PathVariable Integer ticketId) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        return Result.success(ticket);
    }

    /**
     * 更新工单状态
     */
    @Operation(summary = "更新工单状态", description = "更新工单的处理状态")
    @PutMapping("/{ticketId}/status")
    public Result<Boolean> updateTicketStatus(
            @PathVariable Integer ticketId,
            @RequestParam Integer status) {
        boolean result = ticketService.updateTicketStatus(ticketId, status);
        return Result.success(true, "工单状态更新成功");
    }

    /**
     * 分配工单给处理人
     */
    @Operation(summary = "分配工单", description = "将工单分配给指定处理人")
    @PutMapping("/{ticketId}/assign")
    public Result<Boolean> assignTicket(
            @PathVariable Integer ticketId,
            @RequestParam String assignedTo) {
        boolean result = ticketService.assignTicket(ticketId, assignedTo);
         return Result.success(true, "工单分配成功");  
    }

    /**
     * 根据状态获取工单列表
     */
    @Operation(summary = "根据状态获取工单", description = "根据工单状态获取工单列表")
    @GetMapping("/status/{status}")
    public Result<List<Ticket>> getTicketsByStatus(@PathVariable Integer status) {
        List<Ticket> tickets = ticketService.getTicketsByStatus(status);
        return Result.success(tickets);
    }

    /**
     * 根据用户ID获取工单列表
     */
    @Operation(summary = "根据用户ID获取工单", description = "根据用户ID获取该用户提交的工单列表")
    @GetMapping("/user/{userId}")
    public Result<List<Ticket>> getTicketsByUserId(@PathVariable String userId) {
        List<Ticket> tickets = ticketService.getTicketsByUserId(userId);
        return Result.success(tickets);
    }
}
