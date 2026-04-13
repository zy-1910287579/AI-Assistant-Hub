package com.storm.ai.service;


import java.util.List;

public interface VectorDocumentManagerService {

    /**
     * 根据物理主键 ID 列表批量删除文档
     * @param documentIds 要删除的文档 ID 列表
     */
    void removeDocumentsByIds(List<String> documentIds);

    /**
     * 根据用户 ID 和会话 ID 删除文档
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void removeDocumentsByUserAndSession(String userId, String sessionId);



    /**
     * 检查指定文件是否已存在于数据库中
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param fileName 文件名
     * @return 如果存在则返回 true，否则返回 false
     */
    boolean checkFileExists(String userId, String sessionId, String fileName);


    /**
     * 查询指定会话下的所有文档ID
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 文档ID列表
     */
    List<String> findDocumentIdsByUserAndSession(String userId, String sessionId);

}
