package com.storm.tools;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RagAssistanceTools {
    private final VectorStore myPgVectorStore; // 注入 VectorStore

    /**
     * 工具方法: 查询知识库
     * 此方法用于检索与用户问题相关的公司政策、FAQ或其他非用户特定的知识。
     */
    @Tool(description = "Useful for finding company policies, FAQ, or general knowledge when the user asks about rules, procedures, or non-user-specific information.")
    public String queryKnowledge(@ToolParam(description = "The user's question that requires looking up general information or policies.") String question) {

        log.info("RAG Tool called with question: {}", question);

        // 1. 构建搜索请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question) // 使用用户的问题作为查询
                .topK(10) // 获取最相关的3个结果
                .similarityThreshold(0.5) // 设置相似度阈值，过滤掉太不相关的
                .build();

        // 2. 执行相似度搜索
        List<Document> documents = this.myPgVectorStore.similaritySearch(searchRequest);

        // 3. 提取并拼接结果
        if (documents.isEmpty()) {
            log.info("RAG Tool found no relevant documents for: {}", question);
            return "根据现有资料，我无法回答该问题。"; // 返回一个清晰的反馈
        }
        StringBuilder result = new StringBuilder();
        for (Document doc : documents) {
            // 将每个检索到的文档内容追加到结果中
            // 可以选择性地加上元数据，如来源，但这取决于主模型如何处理
            result.append(doc.getText()).append("\n---\n"); // 用分隔符分开多个片段
        }

        String finalResult = result.toString();
        log.info("RAG Tool retrieved content: {}", finalResult);
        return finalResult;

    }
}
