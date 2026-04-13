package com.storm.ai.service;

import org.springframework.ai.document.Document;

import java.util.List;

public interface TransformDocumentToVectorService {


    List<Document> loadAndSplit(String filePath, String originalFilename, String userId, String sessionId);

    void ingestFileToVectorStore(String filePath,String originalFilename,String userId,String sessionId);
}
