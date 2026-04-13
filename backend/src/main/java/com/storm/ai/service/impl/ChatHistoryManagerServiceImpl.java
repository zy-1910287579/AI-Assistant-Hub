// package com.storm.service;
package com.storm.ai.service.impl;

import com.storm.common.enums.ErrorCode;
import com.storm.common.exception.BusinessException;
import com.storm.dto.ChatHistoryItem;
import com.storm.mapper.OurChatHistoryMapper;
//import com.storm.ai.rag.repository.MyChatHistoryRepository;
import com.storm.ai.service.ChatHistoryManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryManagerServiceImpl implements ChatHistoryManagerService {

    private final OurChatHistoryMapper ourChatHistoryMapper;

    //private final MyChatHistoryRepository chatHistoryRepository;

    /**
     * 根据 conversationId 获取完整的对话历史（问答对列表）
     */
    public List<ChatHistoryItem> getChatHistory(String userId,String conversationId) {

        String uniqueId = userId + "_" + conversationId;
        log.debug("加载对话历史: {}", uniqueId);

        // 直接查出来就是组装好的列表！
        // 不需要任何 Java 循环逻辑
        List<ChatHistoryItem> chatHistoryItems = ourChatHistoryMapper.selectChatHistoryItems(uniqueId);
        if(chatHistoryItems == null){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "对话历史查询失败");
        }
        return chatHistoryItems;
    }


    //根据userId和sessionId删除聊天记录
    @Override
    public void deleteHistoryByConversationId(String userId,String conversationId) {

        String uniqueId=userId+"_"+conversationId;
        log.info("正在删除会话 [{}] 的历史记录。", uniqueId);
        // 检查会话是否存在
        if (ourChatHistoryMapper.selectByConversationId(uniqueId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        }
        
        int deletedCount = ourChatHistoryMapper.deleteByConversationId(uniqueId);
        
        if (deletedCount == 0) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "聊天记录删除异常");
        }
        log.info("成功删除 {} 条历史记录。", deletedCount);
    }
    @Override
    public List<ChatMemory> getHistoryByConversationId(String userId,String conversationId) {
        String uniqueId=userId+"_"+conversationId;
        log.debug("正在查询会话 [{}] 的历史记录。", uniqueId);
        // 检查会话是否存在
        if (ourChatHistoryMapper.selectByConversationId(uniqueId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        }

        List<ChatMemory> chatHistoryItems = ourChatHistoryMapper.selectByConversationId(uniqueId);
        if(chatHistoryItems == null){
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "会话历史记录查询失败");
        }
        return chatHistoryItems;
    }

}