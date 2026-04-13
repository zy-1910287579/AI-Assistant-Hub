package com.storm.ai.config;

import com.storm.common.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ChatMemoryConfiguration {
/**JdbcChatMemoryRepository 在存取消息时，底层使用了 Spring 的 ObjectMapper（Jackson），
 把 Message 对象（如 UserMessage, AiMessage）转成 JSON 字符串，存入数据库的 content 字段。
 对于简单消息（只有文本），content 就是原始字符串
 但对于复杂消息（带 metadata、多模态等），它会存成 JSON*/
    /**这里根据yml文件里的pg数据库配置,会自动注入pg的实现,
     jdbcChatMemoryRepository是非内存存储,必须要指定数据库
     **/


    private  final JdbcChatMemoryRepository jdbcChatMemoryRepository;
    @Bean
    public ChatMemory chatMemory(){
        log.info("对话记忆初始化成功!");
        /**配ChatMemory的各种参数
         * 1.ChatMemory的具体实现类,都是builder工厂创建
         * 2.具体实现类的的自定义存储类
         * 3.最大滑动窗口大小
         * 4.返回创建好的ChatMemory对象
         * */
         return  MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(AppConstants.AI_MAX_MESSAGES)
                .build();
    }
}
