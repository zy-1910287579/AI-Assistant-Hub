package com.storm.entity;

import lombok.Data;

/**
 * 用于映射 vector_store 表的实体类
 * 代表一个文档块的元数据信息
 */
@Data
public class VectorDocument {

    // 与 vector_store 表的主键 'id' 对应
    private String id;

    // 存储向量的列，通常为 'embedding'
    private Object embedding; // 在实际操作中，我们很少直接通过 MyBatis 读写这个字段

    // 存储元数据的列，通常为 'metadata'，对应 JSON 类型
    private String metadata; // 可以是 JSON 字符串


}