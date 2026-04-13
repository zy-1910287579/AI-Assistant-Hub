package com.storm.ai.controller;
import com.storm.common.vo.Result;
import com.storm.ai.service.VectorDocumentManagerService;
import com.storm.ai.service.TransformDocumentToVectorService;
import com.storm.context.UserContext;
import com.storm.entity.TokenPayload;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
@Validated
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/store")
@RestController
@Tag(name = "向量库文件操作模块")
public class VectorDocumentController {
    private final VectorDocumentManagerService vectorDocumentManagerService; // 注入服务
    private final TransformDocumentToVectorService transformDocumentToVectorService;

    @Value("${file.upload.path:./uploads}")  // 从配置文件读取上传路径，默认为 ./uploads
    private String uploadPath;
    /**
     * 根据用户 ID 和会话 ID 清空一个会话中的所有文档
     *
     * 前端请求示例:
     * URL: DELETE /store/session
     * Method: DELETE
     * Content-Type: application/json
     * Request Body: {"userId": "user_123", "sessionId": "session_xyz"}
     */
    //优点：
    //扩展性强：如果未来需要增加更多参数（例如 fileType、timestamp），
    // 只需要在 ClearSessionRequest 对象中添加新字段，Controller 方法签名不变。
    //结构化：参数被打包成一个对象，逻辑上更内聚，特别是在参数较多时。
    //据用户 ID 和会话 ID 清空一个会话中的所有文档
    @Operation(summary = "根据用户id和会话id列表删除向量库数据")
    @DeleteMapping("session")
    public Result<String> clearSession(
            @NotBlank(message = "会话ID不能为空")@RequestParam String sessionId) {
        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();
        log.info("收到清空会话请求，userId: {}, sessionId: {}", userId, sessionId);
        vectorDocumentManagerService.removeDocumentsByUserAndSession(userId, sessionId);
        log.info("成功清空用户 [{}] 会话 [{}] 中的所有文档。", userId, sessionId);

        // 步骤 2: 删除对应文件
        deleteFilesByUserAndSession(userId, sessionId);
        return Result.success("成功清空会话中的文档。", "清空成功");

    }


    /**
     * 根据用户ID和会话ID删除对应的上传文件
     */
    private void deleteFilesByUserAndSession(String userId, String sessionId) {
        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists() || !uploadDir.isDirectory()) {
                log.warn("上传目录不存在或不是目录");
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

    /**
     * 检查指定文件是否已存在于数据库中
     *
     * 前端请求示例:
     * URL: GET /store/check-file-exists?userId=user_123&sessionId=session_xyz&fileName=manual.pdf
     * Method: GET
     */
    //检查指定文件是否已存在于数据库中
    @Operation(summary = "根据用户id,会话id,文件名来检查文件是否已经存在")
    @GetMapping("check-file-exists")
    public Result<Boolean> checkFileExists(
            @NotBlank(message = "会话ID不能为空")@RequestParam String sessionId,
            @NotBlank(message = "文件名不能为空")@RequestParam String fileName) {
        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();
        log.info("收到文件存在性检查请求，userId: {}, sessionId: {}, fileName: {}", userId, sessionId, fileName);

        boolean exists = vectorDocumentManagerService.checkFileExists(userId, sessionId, fileName);
        return Result.success(exists, "查询成功");
    }


    @Operation(summary = "获取指定会话中已上传的文件列表")
    @GetMapping("list")
    public Result<List<String>> listDocuments(
            @NotBlank(message = "会话ID不能为空") @RequestParam String sessionId) {
        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();

        List<String> fileNames = new ArrayList<>();
        Path uploadDir = Paths.get(uploadPath);
        File[] files = uploadDir.toFile().listFiles((dir, name) -> name.startsWith(userId + "_" + sessionId + "_"));

        if (files != null) {
            for (File file : files) {
                // 提取原始文件名部分 (去掉 userId_sessionId_ 前缀)
                String originalName = file.getName().substring((userId + "_" + sessionId + "_").length());
                fileNames.add(originalName);
            }
        }

        return Result.success(fileNames);
    }

    @Operation(summary = "预览/下载指定的已上传文件")
    @GetMapping("preview/{filename}")
    public ResponseEntity<Resource> previewDocument(
            @NotBlank(message = "会话ID不能为空") @RequestParam String sessionId,
            @Parameter(description = "文件名", required = true) @PathVariable String filename) throws IOException {
        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();

        // 构建安全的文件名以防止路径遍历
        String safeFilename = Paths.get(filename).normalize().toString();
        if (!safeFilename.equals(filename)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        String fullFilename = userId + "_" + sessionId + "_" + safeFilename;
        Path filePath = Paths.get(uploadPath).resolve(fullFilename);
        File file = filePath.toFile();

        if (!file.exists() || !file.canRead()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        String contentType = getContentTypeForFile(file); // 获取MIME类型

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"") // inline 用于浏览器预览
                .body(resource);
    }




    /**
     * 根据文件扩展名推断MIME类型，用于浏览器预览
     */
    private String getContentTypeForFile(File file) {
        String path = file.getPath().toLowerCase();
        if (path.endsWith(".pdf")) {
            return "application/pdf";
        } else if (path.endsWith(".txt")) {
            return "text/plain";
        } else if (path.endsWith(".doc")) {
            return "application/msword";
        } else if (path.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (path.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (path.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (path.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        } else if (path.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (path.endsWith(".png")) {
            return "image/png";
        } else if (path.endsWith(".gif")) {
            return "image/gif";
        } else if (path.endsWith(".bmp")) {
            return "image/bmp";
        } else {
            // 如果无法识别，返回 application/octet-stream，浏览器会提示下载
            return "application/octet-stream";
        }
    }

    @Operation(summary = "下载指定用户的会话窗口的前端文件并向量化入库")
    @PostMapping("upload")
    public Result<Void> uploadDocument(@NotNull(message = "文件不能为空") @RequestParam("file") MultipartFile file, @NotBlank(message = "会话ID不能为空")@RequestParam String sessionId) throws IOException {
        TokenPayload tokenPayload = UserContext.getUser();
        String userId = tokenPayload.getUserId();
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        // 3. 生成唯一文件名,MultipartFile.getOriginalFilename() 方法返回的是客户端上传文件的完整原始文件名，其中包含文件后缀
        String originalFilename = file.getOriginalFilename();

        if(vectorDocumentManagerService.checkFileExists(userId,sessionId,originalFilename)){
            return Result.success("知识库文档已经上传成功过了哦！在项目 uploads 目录下。📚✨");
        }

        // 1. 【修改点】定义当前目录下的文件夹路径
        // "./uploads" 表示在项目运行根目录下创建一个 uploads 文件夹
        Path uploadDir = Paths.get(uploadPath);
            // 2. 【修改点】自动创建目录（如果不存在）
            // createDirectories 会检查目录是否存在，不存在则创建，包括父目录
        Files.createDirectories(uploadDir);

        String uniqueFileName = userId+"_"+sessionId+"_"+originalFilename;

        // 4. 拼接最终的文件完整路径
        Path filePath = uploadDir.resolve(uniqueFileName);


        // 5. 保存文件
        Files.write(filePath, file.getBytes());

        // 6. 调用服务将文件内容分块并存入向量库
        // 这里传入绝对路径，确保服务能准确找到文件
        transformDocumentToVectorService.ingestFileToVectorStore(filePath.toAbsolutePath().toString(),originalFilename,userId,sessionId);

        log.info("文件 {} 成功上传并保存到: {}", originalFilename, filePath.toAbsolutePath());
        return Result.success("知识库文档上传成功！文件已保存在项目 uploads 目录下。📚✨");

    }



}
