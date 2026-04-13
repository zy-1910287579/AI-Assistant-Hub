package com.storm.ai.config;
import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import com.storm.common.constants.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
@Slf4j
@Configuration
public class DocumentHandlerConfiguration {

    /*
    TODO 实习被问好例子之一----------------------------2.3步为何运行速度如此之慢???,是什么问题? 怎么解决的?? 学到了什么?
     1.因为调用大模型次数很多,到底运行时间不合理增加
     切分  ：   本地计算 → 毫秒级
     关键词：  4 chunks × 1 次 = 4 次 LLM 调用,因为每一个chunk都需要调用大模型总结关键词,时间是chunk.size()
     摘要  ：   4 chunks × ~2.5 次 = 10 次 LLM 调用,同理,每个chunk还需要上下文,故时间是大约3*chunk.size()
     解决方法一:只保留步骤 1+2
     KeywordMetadataEnricher 对rag价值高（提升检索相关性、支持关键词过滤）
     SummaryMetadataEnricher 中低（当前摘要有用，prev/next 在预处理阶段几乎无用）	切耗时极高（2~3×N 次调用）
     并且将每块关键词改为一个!
     解决方法二:RetrievalAugmentationAdvisor使用,它提供了更灵活和强大的 RAG 实现--
     --如查询转换、文档检索、文档后处理、查询增强等）来构建定制化的 RAG 流程。
     */
    /*开始引入流水线加工chunk来更进准的实现文本召回*/
    @Bean
    public DocumentTransformer recursiveSplitter(){
        // 定义分隔符数组，优先级从高到低
        String[] separators = new String[]{
                "。",   // 中文句号
                "！",   // 中文感叹号
                "？",   // 中文问号
                "；",   // 中文分号
                "，",   // 中文逗号
                ".",    // 英文句号
                "!",    // 英文感叹号
                "?",    // 英文问号
                ";",    // 英文分号
        };

        // 创建阿里云的 RecursiveCharacterTextSplitter 实例
        RecursiveCharacterTextSplitter recursiveSplitter = new RecursiveCharacterTextSplitter(
                AppConstants.RAG_CHUNK_SIZE, // chunkSize
                separators // separators
        );

        log.info("阿里云 RecursiveCharacterTextSplitter 初始化成功! ChunkSize: {}", AppConstants.RAG_CHUNK_SIZE);
        return recursiveSplitter;
    }


    // ========================
    // 1. 精准文本分割器
    // ========================
    @Bean
    public DocumentTransformer textSplitter() {
        /**
         * .withChunkSize(500) 不是“必须严格 500 tokens”，而是“目标大小约 500 tokens”。
         .withPunctuationMarks(...) 的作用就是：为了句子完整，允许 chunk 实际长度偏离 500 —— 可能 480，也可能 520，甚至 600。*/
        /**
         • minChunkLengthToEmbed=50
         • 更全的标点（含引号、省略号）
         • maxNumChunks=5000 安全兜底
         防止噪声 chunk；提升中英文混合文本断句质量；避免内存溢出*/
        TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(AppConstants.RAG_CHUNK_SIZE)
                .withMinChunkSizeChars(AppConstants.RAG_MIN_CHUNK_SIZE_CHARS) // 最小字符数，避免碎片
                .withMaxNumChunks(AppConstants.RAG_MAX_NUM_CHUNKS)    // 防止超大文档 OOM,最大块数5000个
                .withPunctuationMarks(List.of(
                                '。', '？', '！', '；', '…', '”', '’',
                                '.', '?', '!', ';', ':', '"', '\''
                        )
                )
                .withKeepSeparator(true)
                .build();
        log.info("文本分割器初始化成功!");
        return textSplitter;
    }

    // ========================
    // 2. 关键词增强器（每 chunk 提取 5 个关键词）
    // ========================
    /**
     * 显式提取 5 个关键词，自动注入 excerpt_keywords
     * 提升向量检索相关性，支持关键词过滤聚类*/
    /**@Bean
    public DocumentTransformer keywordEnricher(OpenAiChatModel openAiChatModel) {

        KeywordMetadataEnricher keywordEnricher = KeywordMetadataEnricher.builder(openAiChatModel)
                .keywordCount(5)  // 提取 5 个唯一关键词
                .build();
        log.info("关键词增强器初始化成功!");
        return keywordEnricher;
        // 可选：自定义模板（如需控制格式或语言）
        // .keywordsTemplate(new PromptTemplate("从以下文本中提取5个中文关键词，用逗号分隔：\n{context_str}"))
    }*/

    // ========================
    // 3. 上下文摘要增强器（支持前后文联动）
    // ========================

    /**
     * 启用 PREVIOUS/CURRENT/NEXT 三重摘要
     * 使每个 chunk 具备“上下文感知能力”，极大提升问答连贯性（尤其在长文档中）*/
//    @Bean
//    public DocumentTransformer summaryEnricher(OpenAiChatModel openAiChatModel) {
//        SummaryMetadataEnricher summaryEnricher = new SummaryMetadataEnricher(
//                openAiChatModel,
//                List.of(
//                       SummaryMetadataEnricher.SummaryType.PREVIOUS,
//                        SummaryMetadataEnricher.SummaryType.CURRENT,
//                        SummaryMetadataEnricher.SummaryType.NEXT
//                )
//        );
//        log.info("上下文摘要增强器初始化成功!");
//        return summaryEnricher;
//    }

}
