package com.storm.common.constants;


/**
 * 应用常量类
 * 用于集中管理系统的配置参数、魔法值、业务规则阈值等。
 * 避免在代码中硬编码数字或字符串。
 */
public final class AppConstants {


    // 私有构造器，防止被实例化
    private AppConstants() {
        throw new IllegalStateException("Constant class");
    }

    // ================= AI 模型参数 =================
    /**
     * 大模型记忆滑动窗口大小 (Context Window)
     * 用户提问和ai回答算俩条
     */
    public static final int AI_MAX_MESSAGES = 20;

    /**
     * 大模型生成的最大 Token 数
     */
    public static final int AI_MAX_TOKENS = 2048;

    /**
     * 模型温度 (Temperature): 0.0 - 1.0
     * 0.0 代表严谨（适合客服），1.0 代表发散（适合创作）
     */
    public static final double AI_TEMPERATURE = 0.0;

    // ================= RAG 文档处理参数 =========================================================================================


    //-------入库------------------------------------------
    /**
     *切分后一次性入库大小,有的嵌入模型有大小限制
     */
    public static final int RAG_MAX_BATCH_SIZES=10;

    /**
     * 文档切分块大小 (Chunk Size)
     * 单位：字符数。通常设置为 500-1000，取决于模型对上下文的理解能力
     */
    public static final int RAG_CHUNK_SIZE = 1000;

    /**
     * 切分重叠大小 (Chunk Overlap)
     * 单位：字符数。保留一部分重叠以保持语义连贯性，通常为 ChunkSize 的 10%-20%
     */
    public static final int RAG_CHUNK_OVERLAP = 64;

    /**
     * 最小字符数放在碎片化
     */
    public static final int RAG_MIN_CHUNK_SIZE_CHARS =50;

    /**
     * 防止超大文档 OOM,最大块数5000个
     */
    public static final int RAG_MAX_NUM_CHUNKS=5000;

//-------出库(检索)--------------------------------------------
    /**
     * 是否允许空上下文
     */
    public static final boolean RAG_ALLOW_EMPTY_CONTEXT=true;
    /**
     * 向量检索 Top K (检索数量)
     * 每次去向量数据库查询时，返回最相关的 N 个文档片段
     */
    public static final int RAG_TOP_K = 8;

    /**
     * 向量相似度阈值
     * 低于这个分数的文档片段将被过滤掉（0-1之间，视具体向量库算法而定）
     */
    public static final double RAG_SIMILARITY_THRESHOLD = 0.65;


    // ================= 业务规则常量 =================

    public static final String USER="USER";

    public static final String ASSISTANT="ASSISTANT";

    /**
     * 用户会话过期时间 (分钟)
     */
    public static final long SESSION_EXPIRE_MINUTES = 30L;

    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 100;

}
