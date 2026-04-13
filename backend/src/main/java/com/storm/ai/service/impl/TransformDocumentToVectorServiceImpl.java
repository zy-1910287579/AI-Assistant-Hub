package com.storm.ai.service.impl;


import com.storm.ai.service.TransformDocumentToVectorService;
import com.storm.common.constants.AppConstants;
import com.storm.common.enums.ErrorCode;
import com.storm.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransformDocumentToVectorServiceImpl implements TransformDocumentToVectorService {


    /*!!!!!!!!!!!!!!!!!!!!!!!!这里我将使用模型评估来检测rag功能!!!!!!!!!!!!!!!!!!!!!2026-3-25*/
    private final PgVectorStore vectorStore;
    private final DocumentTransformer recursiveSplitter;

    public List<Document> loadAndSplit(String filePath,String originalFilename,String userId,String sessionId) {
        Resource resource = new FileSystemResource(filePath);
        //核心转换!,文件转document对象列表
        //这里有一个学习点,如果你点击去TikaDocumentReader源码,你会发现里面并,欸有实现read方法,而是
       //调用的是接口里的read方法,read方法的返回值是get方法,所以这里调用的是get方法
        //1.读取
        List<Document> rawDocs = new TikaDocumentReader(resource).read();
        /*
        public TikaDocumentReader(String resourceUrl) {
        this(resourceUrl, ExtractedTextFormatter.defaults());

        在这段代码中，this(resourceUrl, ExtractedTextFormatter.defaults());
        的作用是在一个构造方法中调用同一个类的另一个构造方法。

        }*/

        /*TODO:实习被问好例子之二,---------------------------------在对文档加元数据的时候遇到了官方文档描述不清晰的情况
        *  1.解决方案,对文档阅读一边,检查是否遗落了知识点
        *  2.看之前的初级项目-----了解到和Document类有关
        *  3.看Document源码并结合ai解答成功解决了问题!!!!!哦耶
        *  4.有多种解决方案,我选择了最合适的哪一个,先给所有文档加元数据再分块
        * */

        //2.切分
        List<Document> chunks = recursiveSplitter.apply(rawDocs);
        List<Document> processedDocs = new ArrayList<>();

        if(chunks.size()>1000){
            throw new BusinessException(ErrorCode.RAG_STORGE_ERROR,"文档内容过大,分块过多!");
        }

        // 遍历分块后列表，使用 mutate() 修改元数据
        //在分块后的列表里加元数据是最好的
        // --- 修改点：使用传统 for 循环，确保索引 i 准确递增 ---
        for (int i = 0; i < chunks.size(); i++) {
            Document document = chunks.get(i);

            // --- A. 获取块索引 ---
            // 直接用循环变量 i，这就是最准确的 0, 1, 2...
            String chunkIndex = String.valueOf(i);

            // --- B. 清洗文件名 ---
            // 把 "test.pdf" 变成 "test_pdf"，防止 ID 包含非法字符
            String safeFileName = originalFilename.replaceAll("[^a-zA-Z0-9]", "_");

            // --- C. 构造唯一主键 ID ---
            // 格式：sessionid_filename_chunkIndex
            // 例如：rag_001_manual_pdf_0
            String rawId = userId + "_" + sessionId + "_" + safeFileName + "_" + chunkIndex;

            // 将其转换为标准的 UUID (Type 3/5 风格)
            // 这会把 "rag_001_..." 这种长字符串压缩成一个标准的 32位 UUID
            UUID safeUuid = UUID.nameUUIDFromBytes(rawId.getBytes());
            String uniqueId = safeUuid.toString();

            // --- D. 构建最终文档 ---
            Document finalDoc = document.mutate()
                    .id(uniqueId) // 设置物理主键，防止覆盖
                    .metadata("user_id", userId)
                    .metadata("session_id", sessionId)
                    .metadata("file_name", originalFilename)
                    .metadata("source", "user_upload")
                    .metadata("processed_date", System.currentTimeMillis())
                    .metadata("chunk_index", chunkIndex) // 建议：把 chunkIndex 也存进 metadata，方便以后调试查看
                    .build();
            processedDocs.add(finalDoc);
        }

        log.info("✅ 处理完成: 文件 [{}], 用户 [{}], 窗口 [{}], 共切分 {} 块",
                originalFilename, userId, sessionId, processedDocs.size());


        return processedDocs;
        }
    public void ingestFileToVectorStore(String filePath,String originalFilename,String userId,String sessionId) {
        //去重操作前端controller层已验证;
        List<Document> documents = loadAndSplit(filePath,originalFilename,userId,sessionId);

        log.info("🚀 开始存入向量库，共 {} 块", documents.size());
        // 检查向量库是否存在

        addDocumentsInBatches(documents, AppConstants.RAG_MAX_BATCH_SIZES); //
    }
    //原check辅助方法已删除,移交mybatisService层
    /**
     * 分批添加文档到向量库（适配阿里云 embedding 批量限制）
     */
    private void addDocumentsInBatches(List<Document> documents, int batchSize) {
        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<Document> batch = documents.subList(i, end);
            vectorStore.add(batch); // 每次 ≤10 个
            System.out.println("已入库批次: " + (i / batchSize + 1) + ", 文档数: " + batch.size());
        }
    }
}
