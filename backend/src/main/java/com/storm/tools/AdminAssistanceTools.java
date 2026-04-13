package com.storm.tools;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storm.entity.Order;
import com.storm.entity.Ticket;
import com.storm.entity.User;
import com.storm.mapper.AdminMapper;
import com.storm.service.OrderService;
import com.storm.service.TicketService;
import com.storm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Component
public class AdminAssistanceTools {

    private final UserService userService;
    private final TicketService ticketService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final AdminMapper adminMapper;


    // ==================== 用户管理工具 ====================
    /**
     * 工具：查询所有用户或根据用户名搜索
     */
    @Tool(description = "查询系统内所有用户信息，或者根据用户名模糊搜索特定用户。如果不提供用户名，则返回所有用户列表（注意数据量可能较大）。")
    public String queryUsers(
            @ToolParam(description = "可选的用户名搜索关键词，如果为空则查询所有用户") String usernameKeyword) {

        List<User> users;
        if (StringUtils.hasText(usernameKeyword)) {
            // 模拟模糊查询，实际需 UserService 支持，这里直接用 MP  wrapper 演示
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(User::getUsername, usernameKeyword);
            users = userService.list(wrapper);
        } else {
            users = userService.list();
        }

        if (users.isEmpty()) {
            return "未找到相关用户";
        }

        // 脱敏：管理员查询时也不应直接看到密码
        users.forEach(u -> u.setPassword(null));

        try {
            return objectMapper.writeValueAsString(users);
        } catch (JsonProcessingException e) {
            log.error("用户数据序列化失败", e);
            return "用户数据序列化失败";
        }
    }

    /**
     * 工具：更新用户状态（封禁/解封）
     */
    @Tool(description = "更新指定用户的账号状态。常用于封禁违规用户或解封账号。")
    public String updateUserAccountStatus(
            @ToolParam(description = "目标用户的ID") String userId,
            @ToolParam(description = "新的账号状态 (例如: 0-正常, 1-封禁)") Integer status) {

        if (!StringUtils.hasText(userId)) {
            return "操作失败：用户ID不能为空";
        }

        boolean success = userService.updateUserStatus(userId, status);
        if (success) {
            return String.format("用户 %s 的状态已成功更新为 %d", userId, status);
        } else {
            return "操作失败：用户不存在或更新过程出错";
        }
    }

    /**
     * 工具：更新用户 VIP 等级
     */
    @Tool(description = "手动调整指定用户的 VIP 等级。")
    public String updateUserVipLevel(
            @ToolParam(description = "目标用户的ID") String userId,
            @ToolParam(description = "新的 VIP 等级数值") Integer vipLevel) {

        if (!StringUtils.hasText(userId)) {
            return "操作失败：用户ID不能为空";
        }

        boolean success = userService.updateVipLevel(userId, vipLevel);
        if (success) {
            return String.format("用户 %s 的 VIP 等级已调整为 %d", userId, vipLevel);
        } else {
            return "操作失败：更新 VIP 等级失败";
        }
    }

    // ==================== 工单管理工具 ====================

    /**
     * 工具：查询工单列表（支持按状态筛选）
     */
    @Tool(description = "查询工单列表。可以根据状态筛选（如：0-待处理, 1-处理中, 2-已解决），如果不传状态则返回所有工单。")
    public String queryTickets(
            @ToolParam(description = "可选的工单状态筛选条件") Integer status) {

        List<Ticket> tickets;
        if (status != null) {
            tickets = ticketService.getTicketsByStatus(status);
        } else {
            tickets = ticketService.getAllTickets();
        }

        if (tickets.isEmpty()) {
            return "未找到符合条件的工单";
        }

        try {
            return objectMapper.writeValueAsString(tickets);
        } catch (JsonProcessingException e) {
            return "工单数据序列化失败";
        }
    }

    /**
     * 工具：分配工单
     */
    @Tool(description = "将指定的工单分配给特定的处理人（客服或技术人员）。")
    public String assignTicketToAgent(
            @ToolParam(description = "工单ID") Integer ticketId,
            @ToolParam(description = "处理人姓名或ID") String assignedTo) {

        if (ticketId == null || !StringUtils.hasText(assignedTo)) {
            return "操作失败：工单ID和处理人不能为空";
        }

        boolean success = ticketService.assignTicket(ticketId, assignedTo);
        if (success) {
            return String.format("工单 %d 已成功分配给 %s", ticketId, assignedTo);
        } else {
            return "分配失败：请检查工单ID是否存在";
        }
    }

    /**
     * 工具：更新工单状态
     */
    @Tool(description = "直接更新工单的处理状态（例如：标记为已解决或已关闭）。")
    public String updateTicketState(
            @ToolParam(description = "工单ID") Integer ticketId,
            @ToolParam(description = "新的状态值 (0-待处理, 1-处理中, 2-已解决, 3-已关闭)") Integer status) {

        if (ticketId == null || status == null) {
            return "操作失败：参数不能为空";
        }

        boolean success = ticketService.updateTicketStatus(ticketId, status);
        if (success) {
            return String.format("工单 %d 的状态已更新为 %d", ticketId, status);
        } else {
            return "更新失败：请检查工单ID是否存在";
        }
    }

    // ==================== 订单管理工具 ====================

    /**
     * 工具：查询订单详情
     */
    @Tool(description = "根据订单ID查询订单的详细信息。")
    public String queryOrderDetail(
            @ToolParam(description = "订单ID") String orderId) {

        if (!StringUtils.hasText(orderId)) {
            return "操作失败：订单ID不能为空";
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            return "未找到该订单";
        }

        try {
            return objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            return "订单数据序列化失败";
        }
    }

    /**
     * 工具：强制取消订单
     */
    @Tool(description = "管理员强制取消指定的订单（通常用于处理异常订单）。")
    public String adminCancelOrder(
            @ToolParam(description = "订单ID") String orderId,
    @ToolParam(description = "用户ID") String userId) {

        if (!StringUtils.hasText(orderId)) {
            return "操作失败：订单ID不能为空";
        }

        boolean success = orderService.cancelOrder(orderId, userId).getData();

        if (success) {
            return String.format("订单 %s 已被管理员强制取消", orderId);
        } else {
            return "取消失败：订单可能已发货或不存在";
        }
    }

    // ==================== 📊 统计与图表数据工具 (新增) ====================

    /**
     * 工具：获取用户VIP等级分布数据
     * 用途：专门为了画饼图或柱状图准备的数据
     */
    @Tool(description = "获取系统内所有用户的 VIP 等级分布统计。返回格式为 JSON 对象，键是 VIP 等级，值是人数。专门用于生成 VIP 占比图表。")
    public String getVipLevelDistribution() {
        // 直接用 SQL 聚合：COUNT(*) GROUP BY vip_level
        List<Map<String, Object>> stats = adminMapper.selectVipLevelStats(); // 新增方法

        Map<String, Long> result = stats.stream()
                .collect(Collectors.toMap(
                        m -> String.valueOf(m.get("vip_level")), // key: 字符串化的等级
                        m -> ((Long) m.get("count"))              // value: 数量
                ));

        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "Vip统计失败";
        }
    }

    /**
     * 工具：获取工单类别分布数据
     */
    @Tool(description = "获取所有工单按类别（Category）的分布统计。")
    public String getTicketCategoryStats() {
        List<Map<String, Object>> stats = adminMapper.selectTicketCategoryStats();
        try {
            return objectMapper.writeValueAsString(stats.stream()
                    .collect(Collectors.toMap(
                            m -> (String) m.get("category"),
                            m -> ((Long) m.get("count"))
                    )));
        } catch (JsonProcessingException e) {
            return "工单按类别（Category）的分布统计失败";
        }
    }

    /**
     * 工具：获取工单状态分布数据
     * 用途：分析工单处理效率，画漏斗图或柱状图
     */
    @Tool(description = "获取工单按状态（Status）的分布统计。返回 JSON 对象，键是状态码(0,1,2,3)，值是数量。")
    public String getTicketStatusStats() {
        List<Ticket> allTickets = ticketService.getAllTickets();

        Map<Integer, Long> stats = allTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));

        // 转换 Key 为 String
        Map<String, Long> result = new HashMap<>();
        stats.forEach((k, v) -> result.put(String.valueOf(k), v));

        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "工单状态统计序列化失败";
        }
    }

    // ==================== 🔍 通用数据查询工具 (新增) ====================

    @Tool(description = "通用数据查询工具。支持 user/order/ticket。默认最多返回 100 条，防止数据过大。")
    public String queryRawData(
            @ToolParam(description = "表名 (user, order, ticket)") String tableName,
            @ToolParam(description = "ID 筛选（可选）") String id) {

        if (!StringUtils.hasText(tableName)) {
            return "错误：必须指定表名";
        }

        try {
            switch (tableName.toLowerCase()) {
                case "user":
                    if (StringUtils.hasText(id)) {
                        User u = userService.getById(id);
                        if (u != null) u.setPassword(null);
                        return objectMapper.writeValueAsString(u);
                    } else {
                        List<User> users = userService.list(new LambdaQueryWrapper<User>().last("LIMIT 100"));
                        users.forEach(u -> u.setPassword(null));
                        return objectMapper.writeValueAsString(users);
                    }
                case "order":
                    if (StringUtils.hasText(id)) {
                        Order o = orderService.getOrderById(id);
                        return objectMapper.writeValueAsString(o);
                    } else {
                        List<Order> orders = orderService.list(new LambdaQueryWrapper<Order>().last("LIMIT 100"));
                        return objectMapper.writeValueAsString(orders);
                    }
                case "ticket":
                    if (StringUtils.hasText(id)) {
                        Ticket t = ticketService.getById(Integer.parseInt(id));
                        return objectMapper.writeValueAsString(t);
                    } else {
                        List<Ticket> tickets = ticketService.getAllTickets();
                        // 如果数据量大，建议这里也加 LIMIT
                        if (tickets.size() > 100) {
                            tickets = tickets.subList(0, 100);
                        }
                        return objectMapper.writeValueAsString(tickets);
                    }
                default:
                    return "不支持的表名。支持: user, order, ticket";
            }
        } catch (Exception e) {
            log.error("通用查询异常", e);
            return "查询失败：" + e.getMessage();
        }
    }

// ==================== 📈 高级数据统计工具 (SQL 聚合优化版) ====================

    /**
     * 工具：获取用户消费金额区间分布
     * 优化点：使用 SQL SUM 和 GROUP BY 直接在数据库层面计算，避免加载所有订单到内存。
     */
    @Tool(description = "获取用户按累计消费金额的区间分布统计。基于订单表计算，分为：低消费(0-500), 中消费(500-2000), 高消费(2000以上)。")
    public String getUserSpendingDistribution() {

        // 直接调用 SQL，返回结果如: [{level_name="低消费", count=100}, ...]
        List<Map<String, Object>> stats = adminMapper.selectUserSpendingStats();


        // 将结果转换为适合图表的格式 (Map<String, Integer>)
        Map<String, Integer> result = stats.stream()
                .collect(Collectors.toMap(
                        m -> (String) m.get("level_name"),
                        m -> ((Long) m.get("count")).intValue()
                ));

        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            return "统计失败";
        }
    }

    /**
     * 工具：获取用户喜爱快递公司占比
     */
    @Tool(description = "获取用户最常使用的快递公司统计。")
    public String getUserFavoriteCourierStats() {
        List<Map<String, Object>> stats = adminMapper.selectCourierStats();
        try {
            return objectMapper.writeValueAsString(stats.stream()
                    .collect(Collectors.toMap(
                            m -> (String) m.get("company_name"),
                            m -> ((Long) m.get("count")).intValue()
                    )));
        } catch (JsonProcessingException e) {
            return "统计失败";
        }
    }
    /**
     * 工具：获取用户积分区间分布（终极优化版）
     */
    @Tool(description = "获取用户按积分数量的区间分布统计。")
    public String getUserPointsDistribution() {
        List<Map<String, Object>> stats = adminMapper.selectUserPointsStats();
        try {
            return objectMapper.writeValueAsString(stats.stream()
                    .collect(Collectors.toMap(
                            m -> (String) m.get("level_name"),
                            m -> ((Long) m.get("count")).intValue()
                    )));
        } catch (JsonProcessingException e) {
            return "统计失败";
        }
    }



}
