package com.storm.mapper;

import com.storm.entity.VectorDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VectorDocumentMapper {

    /**
     * 批量插入文档元数据
     * @param documents 要插入的文档列表
     */
    void batchInsert(@Param("documents") List<VectorDocument> documents);

    /**
     * 根据物理主键 ID 批量删除文档
     * @param ids 要删除的文档 ID 列表
     */
    void deleteByIds(@Param("ids") List<String> ids);


    /**
     * 根据 user_id 和 session_id 删除文档
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void deleteByUserAndSession(@Param("userId") String userId, @Param("sessionId") String sessionId);


    /**
     * 检查指定文件是否已存在于数据库中
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param fileName 文件名
     * @return 记录数量
     */
    int countByUserSessionAndFileName(@Param("userId") String userId, @Param("sessionId") String sessionId,
                                      @Param("fileName") String fileName);



    /**
     * 查询指定会话下的所有文档ID
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 文档ID列表
     */
    List<String> selectIdsByUserAndSession(@Param("userId") String userId, @Param("sessionId") String sessionId);
}

