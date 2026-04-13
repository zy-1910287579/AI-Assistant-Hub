package com.storm.ai.service;

import com.storm.dto.ChatHistoryItem;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.List;

public interface ChatHistoryManagerService {

    List<ChatHistoryItem> getChatHistory(String userId,String conversationId);

    /**
     * 根据 conversationId (由 userId 和 sessionId 拼接) 删除整个会话的历史记录
     * @param conversationId 对话会话ID
     */
    List<ChatMemory> getHistoryByConversationId(String userId,String conversationId);

    /**
     * 根据 conversationId (由 userId 和 sessionId 拼接) 删除整个会话的历史记录
     * @param conversationId 对话会话ID
     */
    void deleteHistoryByConversationId(String userId,String conversationId);


}
