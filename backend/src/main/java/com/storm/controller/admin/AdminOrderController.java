package com.storm.controller.admin;


import com.storm.common.vo.Result;
import com.storm.context.UserContext;
import com.storm.entity.TokenPayload;
import com.storm.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author Storm
 * @since 2026-03-31
 */
@Tag(name = "管理端-订单管理")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    private final OrderService orderService;


    /**
     * 更新订单状态（管理员功能，可根据实际需求调整）
     *
     * @param orderId 订单ID
     * @param status 新状态
     * @return 更新结果
     */
    @Operation(summary = "更新订单状态", description = "更新订单状态（仅限管理员或系统使用）")
    @PutMapping("/{orderId}/status")
    public Result<Boolean> updateOrderStatus(
            @Parameter(description = "订单ID", required = true)
            @PathVariable String orderId,
            @Parameter(description = "新订单状态", required = true)
            @RequestParam Integer status) {
        TokenPayload tokenPayload = UserContext.getUser();
        if (tokenPayload == null) {
            return Result.error("未认证的用户");
        }

        // 在实际应用中，可能需要检查用户角色是否为管理员
        if (!"admin".equals(tokenPayload.getRole())) {
            return Result.error("无权限执行此操作");
        }

        log.info("更新订单状态: orderId={}, status={}, operator={}",
                orderId, status, tokenPayload.getUserId());
        boolean result = orderService.updateOrderStatus(orderId, status);
        return Result.success(true, "订单状态更新成功");        
    }


}
