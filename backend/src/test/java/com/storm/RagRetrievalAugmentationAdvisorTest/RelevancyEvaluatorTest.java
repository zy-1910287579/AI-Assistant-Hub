package com.storm.RagRetrievalAugmentationAdvisorTest;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//JUnit 5 不支持测试类用构造器注入！!!!!
@Slf4j
@SpringBootTest
public class RelevancyEvaluatorTest {
    @Autowired
    private DashScopeChatModel dashScopeChatModel;
    @Autowired//哎呀,单独提取出来用就是舒服!
    private Advisor retrievalAugmentationAdvisor;
    //评估回答是否解决了用户的问题且与上下文一致。
    @Test
    void testRagResponseAccuracy(){
        // 1. 准备问题
        String question = "调研方法有几种?";
        log.info("相关性评估模型初始化成功!,使用模型为:{}", dashScopeChatModel.getClass().getName());

        log.info("被测模型为使用模型为:{}", dashScopeChatModel.getClass().getName());

        String userId="user_A";
        String sessionId="rag_001";


        // 1. 构建 RAG 流程获取响应
        ChatResponse chatResponse = ChatClient.builder(dashScopeChatModel).build()
                .prompt(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId+"_"+sessionId))
                .advisors(retrievalAugmentationAdvisor) // 使用 RAG Advisor
                .call()
                .chatResponse();

        // 2. 构建评估请求
        Assertions.assertNotNull(chatResponse);
        EvaluationRequest evaluationRequest = new EvaluationRequest(

                question, // 用户问题
                chatResponse.getMetadata().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT), //被检索到的上下文
                chatResponse.getResult().getOutput().getText() // AI 单纯的回答,和.call().content()一样
        );

        System.out.println("用户的问题是:"+question);
        System.out.println("Ai的回答是:"+chatResponse.getResult().getOutput().getText());

        // 3. 执行评估
        RelevancyEvaluator evaluator = new RelevancyEvaluator(ChatClient.builder(dashScopeChatModel));
        EvaluationResponse evaluationResponse = evaluator.evaluate(evaluationRequest);

        // 4. 验证结果
        assertThat(evaluationResponse.isPass()).isTrue();

    }
}
