package com.storm.ai.config;


import com.storm.common.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DocumentSelectConfiguration {

    private final VectorStore myPgVectorStore;

    private final ChatModel DashScopeChatModel;

    @Bean
    public Advisor questionAnswerAdvisor(){

        /**标准的 RAG 实现中，检索到的 chunks（上下文）通常 不会 被写入聊天历史！*/
        PromptTemplate customPromptTemplate = PromptTemplate.builder()
                .template("""
        你是一个专业、准确且乐于助人的AI知识助手。
        请根据以下参考资料回答用户的问题：
       
        - 如果参考资料中有明确答案，请用自己的话简洁、清晰地回答，不要直接复制长段落。
        - 如果参考资料中没有相关信息，请明确回答：“根据现有资料，我无法回答该问题。”，**不要编造、推测或引用外部知识**。
        - 回答应聚焦问题本身，避免冗长，一般不超过10句话。
        - 使用正式但友好的中文，不使用表情符号或拟人化语气。

        参考资料：
        {question_answer_context}

        问题：
        {query}
        """).build();

        /**
         * Spring AI 提供了多种检索增强方法，
         * 主要分为基于API的现成流程QuestionAnswerAdvisor
         * 和模块化的自定义流程RetrievalAugmentationAdvisor
         *
         * */




        log.info("QuestionAnswerAdvisor增强器初始化成功!");
        //方式一:基于API的现成流程：QuestionAnswerAdvisor
        return  QuestionAnswerAdvisor.builder(myPgVectorStore)
                //可以通过SearchRequest来限制检索结果，例如设置相似度阈值和返回结果的数量
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(AppConstants.RAG_SIMILARITY_THRESHOLD) // 相似度阈值
                        .topK(AppConstants.RAG_TOP_K)                  // 最多返回2个片段
                        .build())
                //可以通过PromptTemplate自定义如何将检索到的上下文和用户问题合并
                .promptTemplate(customPromptTemplate)
                .build();
    }

    @Bean
    public Advisor retrievalAugmentationAdvisor(){

        // 为 Query Transformation 创建低温度的 ChatClient
        ChatClient lowTempChatClient = ChatClient.builder(DashScopeChatModel)
                .defaultOptions(ChatOptions.builder()
                        .temperature(AppConstants.AI_TEMPERATURE)
                        .build())
                .build();



        //方式二: 模块化自定义流程：RetrievalAugmentationAdvisor
        log.info("RetrievalAugmentationAdvisor增强器初始化成功!");
        return RetrievalAugmentationAdvisor.builder()
                // 1. 检索前：添加查询变换器--三种变换器,使用第二个RewriteQueryTransformer
                //使用大语言模型重写用户查询，使其更适合目标检索系统（如向量数据库）。
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(lowTempChatClient.mutate())
                        .build())

                // 2. 检索：配置文档检索器,还可以在build()前加其他其他参数--动态过滤表达式--文档连接 (Document Joiner)
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(myPgVectorStore)
                        .topK(AppConstants.RAG_TOP_K)
                        .similarityThreshold(AppConstants.RAG_SIMILARITY_THRESHOLD)
                        .build())

                // 3. 检索后：配置查询增强器ContextualQueryAugmenter:
                //这是生成前的最后一步，它将检索到的文档内容作为上下文，与用户原始查询结合，形成最终的提示词发送给大语言模型。
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(AppConstants.RAG_ALLOW_EMPTY_CONTEXT)
                        .build())
                .build();
    }




}
