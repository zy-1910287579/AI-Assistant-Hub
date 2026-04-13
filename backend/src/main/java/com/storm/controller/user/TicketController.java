package com.storm.controller.user;


import com.storm.common.vo.Result;
import com.storm.context.UserContext;
import com.storm.entity.Ticket;
import com.storm.entity.TokenPayload;
import com.storm.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Tag(name = "用户端-工单管理", description = "用户端工单相关操作接口")
@RestController
@RequestMapping("/api/user/ticket")
public class TicketController {

    private final TicketService ticketService;

    /**
     * 获取当前登录用户的所有工单
     */
    @GetMapping
    @Operation(summary = "获取我的工单列表", description = "返回当前登录用户提交的所有工单信息")
    public Result<List<Ticket>> getMyTickets() {
        // 从上下文中获取当前登录用户的信息
        TokenPayload currentUser = UserContext.getUser();
        if (currentUser == null) {
            // 这里返回 401 状态码的 Result，前端可根据 code 判断跳转登录
            return Result.error(401, "用户未登录");
        }
        String userId = currentUser.getUserId();

        // 调用 Service 层获取数据
        List<Ticket> tickets = ticketService.getTicketsByUserId(userId);

        // 使用 Result.success 包装数据
        return Result.success(tickets);

    }

    /**
     * 2. 更新用户自己的工单
     */
    @PutMapping("/{ticketId}")
    @Operation(summary = "更新我的工单", description = "仅允许更新状态为'待处理'的工单")
    public Result<String> updateMyTicket(
            @Parameter(description = "工单ID") @PathVariable Integer ticketId,
            @Valid @RequestBody UpdateTicketRequest request) {

        TokenPayload currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }
        String userId = currentUser.getUserId();

        // 调用 Service 层更新
        ticketService.updateUserTicket(ticketId, userId, request.getCategory(), request.getDescription());
        // 返回成功消息
        return Result.success("工单更新成功");

    }

    /**
     * 3. 取消用户自己的工单
     */
    @DeleteMapping("/{ticketId}")
    @Operation(summary = "取消我的工单", description = "仅允许取消状态为'待处理'的工单")
    public Result<String> cancelMyTicket(
            @Parameter(description = "工单ID") @PathVariable Integer ticketId) {

        TokenPayload currentUser = UserContext.getUser();
        if (currentUser == null) {
            return Result.error(401, "用户未登录");
        }
        String userId = currentUser.getUserId();

        // 调用 Service 层取消
        ticketService.cancelUserTicket(ticketId, userId);
        return Result.success("工单已取消");

    }

    /**
     * 内部类：用于接收更新请求体的 DTO
     * 使用 @Data 自动生成 Getter/Setter，配合 @Valid 校验
     */
    @Data
    static class UpdateTicketRequest {
        @NotBlank(message = "工单类别不能为空")
        private String category;

        @NotBlank(message = "工单描述不能为空")
        private String description;
    }


}
