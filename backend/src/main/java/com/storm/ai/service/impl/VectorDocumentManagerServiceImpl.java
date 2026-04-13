package com.storm.ai.service.impl;

import com.storm.mapper.VectorDocumentMapper;
import com.storm.ai.service.VectorDocumentManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorDocumentManagerServiceImpl implements VectorDocumentManagerService {

    private final VectorDocumentMapper vectorDocumentMapper;

    @Value("${file.upload.path:./uploads}")  // 从配置文件读取上传路径，默认为 ./uploads
    private String uploadPath;

    @Override
    public void removeDocumentsByIds(List<String> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            log.warn("尝试删除文档，但 ID 列表为空。");
            return;
        }
        log.info("正在删除 {} 个文档。", documentIds.size());
        try {
            vectorDocumentMapper.deleteByIds(documentIds);
            log.info("成功删除 {} 个文档。", documentIds.size());
        } catch (Exception e) {
            log.error("删除文档时发生错误，IDs: {}", documentIds, e);
            throw e; // 重新抛出异常，由上层处理
        }
    }


    @Override
    public void removeDocumentsByUserAndSession(String userId, String sessionId) {
        if (userId == null || userId.isBlank() || sessionId == null || sessionId.isBlank()) {
            log.warn("尝试删除文档，但 userId 或 sessionId 为空。");
            return;
        }
        log.info("正在删除用户 [{}] 在会话 [{}] 中的所有文档。", userId, sessionId);

        try {
            // 步骤 1: 查询出所有符合条件的文档 ID
            List<String> documentIds = vectorDocumentMapper.selectIdsByUserAndSession(userId, sessionId);

            if (documentIds.isEmpty()) {
                log.info("用户 [{}] 在会话 [{}] 中没有找到任何文档，无需删除。", userId, sessionId);
                return;
            }

            log.info("找到 {} 个待删除的文档 ID。", documentIds.size());

            // 步骤 2: 复用之前写好的方法，根据 ID 列表进行删除
            removeDocumentsByIds(documentIds);

            log.info("用户 [{}] 在会话 [{}] 中的文档已全部删除。", userId, sessionId);

            // 步骤 2: 删除对应文件
            deleteFilesByUserAndSession(userId, sessionId);

        } catch (Exception e) {
            log.error("根据用户和会话删除文档时发生错误: userId=[{}], sessionId=[{}]", userId, sessionId, e);
            throw e;
        }
    }

    /**
     * 根据用户ID和会话ID删除对应的上传文件
     */
    private void deleteFilesByUserAndSession(String userId, String sessionId) {
        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists() || !uploadDir.isDirectory()) {
                log.warn("上传目录不存在或不是目录: {}", uploadPath);
                return;
            }

            // 构造文件名前缀
            String filePrefix = userId + "_" + sessionId + "_";

            File[] files = uploadDir.listFiles((dir, name) -> name.startsWith(filePrefix));

            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            log.info("成功删除文件: {}", file.getAbsolutePath());
                        } else {
                            log.warn("删除文件失败: {}", file.getAbsolutePath());
                        }
                    }
                }
                log.info("已尝试删除用户 [{}] 会话 [{}] 的 {} 个相关文件。", userId, sessionId, files.length);
            } else {
                log.info("用户 [{}] 在会话 [{}] 中未找到需要删除的文件。", userId, sessionId);
            }
        } catch (Exception e) {
            log.error("删除用户 [{}] 会话 [{}] 相关文件时发生错误", userId, sessionId, e);
            throw e;
        }
    }



    @Override
    public boolean checkFileExists(String userId, String sessionId, String fileName) {
        int count = vectorDocumentMapper.countByUserSessionAndFileName(userId, sessionId, fileName);
        boolean exists = count > 0;
        if(exists){
            log.info("文档已存在: userId=[{}], sessionId=[{}], fileName=[{}]", userId, sessionId, fileName);
        }
        return exists;
    }

    @Override
    public List<String> findDocumentIdsByUserAndSession(String userId, String sessionId) {
        log.debug("查询用户 [{}] 在会话 [{}] 下的所有文档 ID。", userId, sessionId);
        return vectorDocumentMapper.selectIdsByUserAndSession(userId, sessionId);
    }

    // ... 将来可以在这里实现其他方法 ...
}