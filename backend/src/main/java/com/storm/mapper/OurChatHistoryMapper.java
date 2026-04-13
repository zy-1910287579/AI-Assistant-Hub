package com.storm.mapper;

import com.storm.dto.ChatHistoryItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.List;

@Mapper
public interface OurChatHistoryMapper  {

    /**
     * 插入一条对话历史记录
     * @param chatMemory 对话历史实体
     * @return 影响的行数
     */
    int insert(ChatMemory chatMemory);

    /**
     * 根据对话会话ID删除所有历史记录
     * @param conversationId 对话会话ID
     * @return 影响的行数
     */
    int deleteByConversationId(@Param("conversationId") String conversationId);

    /**
     * 根据对话会话ID查询所有历史记录
     * @param conversationId 对话会话ID
     * @return 历史记录列表
     */
    List<ChatMemory> selectByConversationId(@Param("conversationId") String conversationId);

    /**
     * 根据ID列表批量删除记录 (可选，为将来可能的精确删除做准备)
     * @param ids ID列表
     * @return 影响的行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);


    // 在 Mapper 接口中新增这个方法
    List<ChatHistoryItem> selectChatHistoryItems(@Param("uniqueId") String uniqueId);


}
