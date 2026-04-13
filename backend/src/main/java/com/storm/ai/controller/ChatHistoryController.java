package com.storm.ai.controller;

import com.storm.context.UserContext;
import com.storm.dto.ChatHistoryItem;
import com.storm.common.vo.Result;
import com.storm.ai.service.ChatHistoryManagerService;
import com.storm.entity.TokenPayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/history")
@RestController
@Tag(name = "对话历史管理模块",description = "返回对话历史给前端展示")
public class ChatHistoryController {

    private final ChatHistoryManagerService chatHistoryManagerService;
    /**
     * 根据用户 ID 和会话 ID 清空对话历史
     *
     * 前端请求示例 (优化后):
     * URL: DELETE /store/history?userId=user_123&sessionId=session_xyz
     * Method: DELETE
     */
    @Operation(summary = "根据用户和窗口id展示对话历史")
    @GetMapping("getHistoryRecord")
    public Result<List<ChatHistoryItem>> findChatHistory(
                                         @NotBlank(message = "会话ID不能为空")@RequestParam String sessionId){

        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();
        log.info("收到加载对话历史请求，userId: {},sessionId: {}",userId,sessionId );


        List<ChatHistoryItem> chatHistory = chatHistoryManagerService.getChatHistory(userId, sessionId);

        return Result.success(chatHistory);
    }
    @Operation(summary = "根据用户和窗口id展示删除对话历史")
    @DeleteMapping("remove")
    public Result<String> clearChatHistory(

            @NotBlank(message = "会话ID不能为空")@RequestParam String sessionId) { // 接收 userId 和 sessionId

        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();
        log.info("收到清空对话历史请求，userId: {},sessionId: {}",userId,sessionId );


        // 调用服务层方法
        chatHistoryManagerService.deleteHistoryByConversationId(userId,sessionId);
        log.info("成功清空会话 userId: {},sessionId: {}",userId,sessionId );
        return Result.success("清空对话历史成功", "清空成功");

    }


}
