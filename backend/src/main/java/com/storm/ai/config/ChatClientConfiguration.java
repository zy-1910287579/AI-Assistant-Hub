package com.storm.ai.config;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.storm.common.constants.SyStemConstants;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ChatClientConfiguration {

    //其实你可以这样理解,配置类的链式调用是在配初始化参数

    private final Advisor retrievalAugmentationAdvisor;

    private final Advisor questionAnswerAdvisor;

    @Bean
    public ChatClient normalchatClient(DashScopeChatModel DashScopeChatModel, ChatMemory chatMemory, List<McpSyncClient> mcpSyncClients){
        return  ChatClient.builder(DashScopeChatModel)
                .defaultSystem("你古灵精怪,可可爱爱")
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();

    }
    @Bean
    public ChatClient userChatClient(DashScopeChatModel DashScopeChatModel, ChatMemory chatMemory, List<McpSyncClient> mcpSyncClients){
        ChatClient chatClient = ChatClient.builder(DashScopeChatModel)
                .defaultSystem(SyStemConstants.CUSTOMER_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
        log.info("普通对话客户端初始化成功!,使用模型为:{}",DashScopeChatModel.getClass().getName());
        return chatClient;
    }

    @Bean
    public ChatClient adminChatClient(DashScopeChatModel DashScopeChatModel, ChatMemory chatMemory, List<McpSyncClient> mcpSyncClients){
        // 创建 ToolCallbackProvider，它会从MCP客户端中自动发现并暴露所有工具
        SyncMcpToolCallbackProvider toolProvider = SyncMcpToolCallbackProvider.builder()
                .mcpClients(mcpSyncClients)
                .build();
        ChatClient adminChatClient = ChatClient.builder(DashScopeChatModel)
                .defaultSystem(SyStemConstants.ADMIN_SYSTEM_PROMPT)
                .defaultToolCallbacks(toolProvider.getToolCallbacks())
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
        log.info("管理端智能助手客户端初始化成功!,使用模型为:{}",DashScopeChatModel.getClass().getName());
        return adminChatClient;
    }

    @Bean
    public ChatClient ragChatClient(DashScopeChatModel DashScopeChatModel,ChatMemory chatMemory ){
        ChatClient ragChatClient = ChatClient.builder(DashScopeChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        //想用哪种
                        retrievalAugmentationAdvisor  // 👈 RAG 顾问放在这里
                )
                .build();
        log.info("rag对话客户端初始化成功!,使用模型为:{}",DashScopeChatModel.getClass().getName());
        return ragChatClient;
    }

    @Bean
    public ChatClient gameChatClient(DashScopeChatModel DashScopeChatModel, ChatMemory chatMemory){
        ChatClient chatClient = ChatClient.builder(DashScopeChatModel)
                .defaultSystem(SyStemConstants.GAME_SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
        log.info("哄哄模拟器客户端初始化成功!,使用模型为:{}",DashScopeChatModel.getClass().getName());
        return chatClient;
    }

}
