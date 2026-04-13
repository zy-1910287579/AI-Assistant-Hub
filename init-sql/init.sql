create extension if not exists vector;


-- auto-generated definition
create table t_user
(
    user_id        varchar(64)  not null
        primary key,
    username       varchar(128) not null,
    phone_number   varchar(20),
    email          varchar(255),
    vip_level      integer                  default 0,
    points_balance bigint                   default 0,
    created_at     timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at     timestamp with time zone default CURRENT_TIMESTAMP,
    password       varchar(255),
    role           varchar(50)              default 'customer'::character varying,
    status         integer                  default 1
);

create index idx_username
    on t_user (username);

create table spring_ai_chat_memory
(
    id              serial
        primary key,
    conversation_id varchar(36) not null,
    content         text        not null,
    type            varchar(10) not null
        constraint spring_ai_chat_memory_type_check
            check ((type)::text = ANY (ARRAY ['USER'::text, 'ASSISTANT'::text, 'SYSTEM'::text, 'TOOL'::text])),
    timestamp       timestamp   not null
);

create index idx_conversation_id
    on spring_ai_chat_memory (conversation_id);

create index spring_ai_chat_memory_conversation_id_timestamp_idx
    on spring_ai_chat_memory (conversation_id, timestamp);

-- auto-generated definition
create table admin
(
    user_id      varchar(64)  not null
        primary key,
    username     varchar(128) not null,
    phone_number varchar(20),
    email        varchar(255),
    created_at   timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at   timestamp with time zone default CURRENT_TIMESTAMP,
    password     varchar(255),
    role         varchar(50)              default 'admin'::character varying,
    status       integer                  default 1
);

-- auto-generated definition
create table t_order
(
    order_id          varchar(128)                       not null
        primary key,
    user_id           varchar(64)                        not null
        constraint fk_order_user
            references t_user
            on delete cascade,
    product_name      varchar(512)                       not null,
    quantity          integer                  default 1,
    total_amount      numeric(10, 2),
    status            integer                  default 0 not null,
    logistics_company varchar(128),
    tracking_number   varchar(255),
    logistics_info    text,
    shipped_at        timestamp with time zone,
    delivered_at      timestamp with time zone,
    created_at        timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at        timestamp with time zone default CURRENT_TIMESTAMP
);

create index idx_t_order_user_id
    on t_order (user_id);

create index idx_t_order_status
    on t_order (status);


-- auto-generated definition
create table t_ticket
(
    ticket_id   serial
        primary key,
    user_id     varchar(64)                        not null
        constraint fk_ticket_user
            references t_user
            on delete cascade,
    order_id    varchar(128)
        constraint uk_ticket_order_id
            unique
        constraint fk_ticket_order
            references t_order
            on delete cascade,
    category    varchar(64)                        not null,
    title       varchar(255)                       not null,
    description text                               not null,
    priority    integer                  default 1,
    status      integer                  default 1 not null,
    assigned_to varchar(128),
    resolved_at timestamp with time zone,
    closed_at   timestamp with time zone,
    created_at  timestamp with time zone default CURRENT_TIMESTAMP,
    updated_at  timestamp with time zone default CURRENT_TIMESTAMP
);

create index idx_t_ticket_user_id
    on t_ticket (user_id);

create index idx_t_ticket_order_id
    on t_ticket (order_id);

create index idx_t_ticket_status
    on t_ticket (status);

create index idx_t_ticket_category
    on t_ticket (category);



-- auto-generated definition
create table vector_store
(
    id        text not null
        primary key,
    content   text,
    metadata  jsonb,
    embedding vector(1024)
);

create index vector_store_embedding_idx
    on vector_store using hnsw (embedding vector_cosine_ops);

create index idx_vector_store_metadata
    on vector_store using gin (metadata);

create index vector_store_embedding_idx1
    on vector_store using hnsw (embedding vector_cosine_ops);

INSERT INTO t_user (user_id, username, phone_number, email, vip_level, points_balance, password, role, status)
VALUES
('UID001', 'user001', '13800000001', 'user001@test.com', 0, 120, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID002', 'user002', '13800000002', 'user002@test.com', 0, 80, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID003', 'user003', '13800000003', 'user003@test.com', 1, 350, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID004', 'user004', '13800000004', 'user004@test.com', 0, 50, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID005', 'user005', '13800000005', 'user005@test.com', 2, 1200, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID006', 'user006', '13800000006', 'user006@test.com', 0, 90, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID007', 'user007', '13800000007', 'user007@test.com', 1, 420, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID008', 'user008', '13800000008', 'user008@test.com', 0, 30, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID009', 'user009', '13800000009', 'user009@test.com', 3, 3500, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID010', 'user010', '13800000010', 'user010@test.com', 0, 150, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),

('UID011', 'user011', '13800000011', 'user011@test.com', 1, 550, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID012', 'user012', '13800000012', 'user012@test.com', 0, 200, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID013', 'user013', '13800000013', 'user013@test.com', 2, 1800, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID014', 'user014', '13800000014', 'user014@test.com', 0, 45, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID015', 'user015', '13800000015', 'user015@test.com', 1, 620, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID016', 'user016', '13800000016', 'user016@test.com', 0, 110, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID017', 'user017', '13800000017', 'user017@test.com', 3, 4200, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID018', 'user018', '13800000018', 'user018@test.com', 0, 75, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID019', 'user019', '13800000019', 'user019@test.com', 1, 480, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID020', 'user020', '13800000020', 'user020@test.com', 2, 1500, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),

('UID021', 'user021', '13800000021', 'user021@test.com', 0, 130, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID022', 'user022', '13800000022', 'user022@test.com', 1, 520, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID023', 'user023', '13800000023', 'user023@test.com', 0, 60, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID024', 'user024', '13800000024', 'user024@test.com', 2, 1300, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID025', 'user025', '13800000025', 'user025@test.com', 3, 3800, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID026', 'user026', '13800000026', 'user026@test.com', 0, 95, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID027', 'user027', '13800000027', 'user027@test.com', 1, 390, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID028', 'user028', '13800000028', 'user028@test.com', 0, 40, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID029', 'user029', '13800000029', 'user029@test.com', 2, 1600, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID030', 'user030', '13800000030', 'user030@test.com', 0, 180, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),

('UID031', 'user031', '13800000031', 'user031@test.com', 1, 450, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID032', 'user032', '13800000032', 'user032@test.com', 0, 70, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID033', 'user033', '13800000033', 'user033@test.com', 3, 4500, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID034', 'user034', '13800000034', 'user034@test.com', 0, 100, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID035', 'user035', '13800000035', 'user035@test.com', 1, 580, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID036', 'user036', '13800000036', 'user036@test.com', 2, 1900, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID037', 'user037', '13800000037', 'user037@test.com', 0, 55, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID038', 'user038', '13800000038', 'user038@test.com', 1, 410, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID039', 'user039', '13800000039', 'user039@test.com', 0, 140, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID040', 'user040', '13800000040', 'user040@test.com', 2, 1400, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),

('UID041', 'user041', '13800000041', 'user041@test.com', 3, 4000, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID042', 'user042', '13800000042', 'user042@test.com', 0, 85, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID043', 'user043', '13800000043', 'user043@test.com', 1, 510, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID044', 'user044', '13800000044', 'user044@test.com', 0, 65, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID045', 'user045', '13800000045', 'user045@test.com', 2, 1100, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID046', 'user046', '13800000046', 'user046@test.com', 0, 125, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID047', 'user047', '13800000047', 'user047@test.com', 1, 440, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID048', 'user048', '13800000048', 'user048@test.com', 3, 3700, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID049', 'user049', '13800000049', 'user049@test.com', 0, 35, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1),
('UID050', 'user050', '13800000050', 'user050@test.com', 2, 1700, 'e10adc3949ba59abbe56e057f20f883e', 'user', 1);



INSERT INTO t_order (order_id, user_id, product_name, quantity, total_amount, status, logistics_company, tracking_number, logistics_info, shipped_at, delivered_at)
VALUES
-- 订单1-20
('ORD001', 'UID001', '高性能无线蓝牙耳机', 1, 199.00, 3, '顺丰速运', 'SF1000000001', '已送达', '2025-01-01 10:00:00+08', '2025-01-03 14:30:00+08'),
('ORD002', 'UID001', '超薄智能手机钢化膜', 2, 39.90, 1, NULL, NULL, NULL, NULL, NULL),
('ORD003', 'UID002', '10000mAh快充充电宝', 1, 129.00, 4, NULL, NULL, NULL, NULL, NULL),
('ORD004', 'UID002', '笔记本电脑散热支架', 1, 59.00, 3, '中通快递', 'ZT1000000004', '已送达', '2025-01-02 09:15:00+08', '2025-01-04 16:20:00+08'),
('ORD005', 'UID003', '旗舰版游戏鼠标', 1, 299.00, 2, '京东物流', 'JD1000000005', '运输中', '2025-01-05 11:20:00+08', NULL),
('ORD006', 'UID003', '机械键盘青轴', 1, 359.00, 3, '顺丰速运', 'SF1000000006', '已送达', '2025-01-03 13:10:00+08', '2025-01-05 15:45:00+08'),
('ORD007', 'UID004', '高清电脑显示器', 1, 899.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD008', 'UID004', '显示器增高架', 1, 79.00, 3, '圆通速递', 'YT1000000008', '已送达', '2025-01-01 14:00:00+08', '2025-01-04 10:15:00+08'),
('ORD009', 'UID005', '智能手表运动版', 1, 599.00, 3, '顺丰速运', 'SF1000000009', '已送达', '2024-12-30 16:30:00+08', '2025-01-02 09:50:00+08'),
('ORD010', 'UID005', '手表替换表带', 3, 45.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD011', 'UID006', '家用空气净化器', 1, 699.00, 2, '德邦快递', 'DP1000000011', '运输中', '2025-01-04 08:40:00+08', NULL),
('ORD012', 'UID006', '净化器替换滤网', 2, 159.80, 3, '中通快递', 'ZT1000000012', '已送达', '2025-01-02 15:10:00+08', '2025-01-05 11:30:00+08'),
('ORD013', 'UID007', '全自动咖啡机', 1, 1299.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD014', 'UID007', '咖啡胶囊50颗装', 1, 199.00, 3, '顺丰速运', 'SF1000000014', '已送达', '2025-01-03 09:25:00+08', '2025-01-06 14:10:00+08'),
('ORD015', 'UID008', '便携式榨汁杯', 1, 89.00, 0, NULL, NULL, NULL, NULL, NULL),
('ORD016', 'UID008', '电动牙刷成人款', 1, 159.00, 3, '圆通速递', 'YT1000000016', '已送达', '2025-01-01 11:50:00+08', '2025-01-03 16:40:00+08'),
('ORD017', 'UID009', '4K超清投影仪', 1, 1599.00, 3, '京东物流', 'JD1000000017', '已送达', '2024-12-28 10:20:00+08', '2024-12-31 15:25:00+08'),
('ORD018', 'UID009', '投影仪幕布100寸', 1, 299.00, 2, '顺丰速运', 'SF1000000018', '运输中', '2025-01-05 13:30:00+08', NULL),
('ORD019', 'UID010', '家用扫地机器人', 1, 1199.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD020', 'UID010', '扫地机配件套装', 1, 129.00, 3, '中通快递', 'ZT1000000020', '已送达', '2025-01-02 14:05:00+08', '2025-01-04 09:20:00+08'),

-- 订单21-40
('ORD021', 'UID011', '无线充电板', 1, 149.00, 3, '顺丰速运', 'SF1000000021', '已送达', '2025-01-01 09:30:00+08', '2025-01-02 16:15:00+08'),
('ORD022', 'UID011', '车载手机支架', 1, 49.00, 0, NULL, NULL, NULL, NULL, NULL),
('ORD023', 'UID012', '平板电脑10.9英寸', 1, 2499.00, 3, '顺丰速运', 'SF1000000023', '已送达', '2024-12-25 11:10:00+08', '2024-12-28 14:50:00+08'),
('ORD024', 'UID012', '平板保护壳+钢化膜', 1, 89.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD025', 'UID013', '蓝牙音箱低音炮', 1, 259.00, 2, '中通快递', 'ZT1000000025', '运输中', '2025-01-04 10:15:00+08', NULL),
('ORD026', 'UID013', '音箱音频线', 2, 29.90, 3, '圆通速递', 'YT1000000026', '已送达', '2025-01-03 15:40:00+08', '2025-01-05 11:10:00+08'),
('ORD027', 'UID014', '智能门锁指纹锁', 1, 899.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD028', 'UID014', '门锁备用电池', 4, 39.60, 3, '京东物流', 'JD1000000028', '已送达', '2025-01-02 08:50:00+08', '2025-01-03 13:25:00+08'),
('ORD029', 'UID015', '单反相机背包', 1, 229.00, 3, '顺丰速运', 'SF1000000029', '已送达', '2025-01-01 13:20:00+08', '2025-01-04 15:30:00+08'),
('ORD030', 'UID015', '相机清洁套装', 1, 59.00, 2, '中通快递', 'ZT1000000030', '运输中', '2025-01-05 09:45:00+08', NULL),
('ORD031', 'UID016', '家用电动打蛋器', 1, 79.00, 3, '圆通速递', 'YT1000000031', '已送达', '2025-01-02 11:30:00+08', '2025-01-04 14:15:00+08'),
('ORD032', 'UID016', '烘焙工具套装', 1, 109.00, 0, NULL, NULL, NULL, NULL, NULL),
('ORD033', 'UID017', '5G无线路由器', 1, 399.00, 3, '顺丰速运', 'SF1000000033', '已送达', '2024-12-30 14:15:00+08', '2025-01-01 16:30:00+08'),
('ORD034', 'UID017', '路由器千兆网线', 2, 39.80, 1, NULL, NULL, NULL, NULL, NULL),
('ORD035', 'UID018', '行车记录仪高清', 1, 299.00, 2, '京东物流', 'JD1000000035', '运输中', '2025-01-03 10:50:00+08', NULL),
('ORD036', 'UID018', '记录仪内存卡128G', 1, 89.00, 3, '中通快递', 'ZT1000000036', '已送达', '2025-01-01 12:20:00+08', '2025-01-03 09:40:00+08'),
('ORD037', 'UID019', '加湿器大雾量', 1, 159.00, 3, '圆通速递', 'YT1000000037', '已送达', '2025-01-02 13:10:00+08', '2025-01-05 10:25:00+08'),
('ORD038', 'UID019', '加湿器滤芯', 3, 44.70, 1, NULL, NULL, NULL, NULL, NULL),
('ORD039', 'UID020', '电热水壶恒温', 1, 129.00, 3, '顺丰速运', 'SF1000000039', '已送达', '2025-01-01 10:40:00+08', '2025-01-02 15:10:00+08'),
('ORD040', 'UID020', '保温壶2L', 1, 99.00, 4, NULL, NULL, NULL, NULL, NULL),

-- 订单41-60
('ORD041', 'UID021', '筋膜枪肌肉按摩器', 1, 269.00, 3, '顺丰速运', 'SF1000000041', '已送达', '2025-01-02 09:20:00+08', '2025-01-04 13:35:00+08'),
('ORD042', 'UID021', '按摩头替换套装', 1, 49.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD043', 'UID022', '电磁炉家用大功率', 1, 299.00, 2, '中通快递', 'ZT1000000043', '运输中', '2025-01-05 11:10:00+08', NULL),
('ORD044', 'UID022', '电磁炉专用炒锅', 1, 159.00, 3, '圆通速递', 'YT1000000044', '已送达', '2025-01-03 14:25:00+08', '2025-01-06 11:50:00+08'),
('ORD045', 'UID023', '吹风机负离子护发', 1, 199.00, 3, '顺丰速运', 'SF1000000045', '已送达', '2025-01-01 15:30:00+08', '2025-01-03 10:15:00+08'),
('ORD046', 'UID023', '吹风机风嘴套装', 1, 39.00, 0, NULL, NULL, NULL, NULL, NULL),
('ORD047', 'UID024', '电饭煲4L大容量', 1, 399.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD048', 'UID024', '电饭煲不粘内胆', 1, 129.00, 3, '京东物流', 'JD1000000048', '已送达', '2025-01-02 10:10:00+08', '2025-01-04 16:30:00+08'),
('ORD049', 'UID025', '平板电脑蓝牙键盘', 1, 189.00, 3, '顺丰速运', 'SF1000000049', '已送达', '2024-12-29 11:40:00+08', '2024-12-31 14:20:00+08'),
('ORD050', 'UID025', '键盘收纳包', 1, 49.00, 2, '中通快递', 'ZT1000000050', '运输中', '2025-01-04 08:30:00+08', NULL),
('ORD051', 'UID026', '智能体脂秤', 1, 99.00, 3, '圆通速递', 'YT1000000051', '已送达', '2025-01-01 11:15:00+08', '2025-01-02 14:45:00+08'),
('ORD052', 'UID026', '体脂秤电池4节', 1, 15.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD053', 'UID027', '多功能料理锅', 1, 499.00, 3, '顺丰速运', 'SF1000000053', '已送达', '2025-01-03 08:50:00+08', '2025-01-05 15:20:00+08'),
('ORD054', 'UID027', '料理锅烤盘配件', 2, 178.00, 4, NULL, NULL, NULL, NULL, NULL),
('ORD055', 'UID028', '充电宝自带线', 1, 109.00, 3, '京东物流', 'JD1000000055', '已送达', '2025-01-02 13:40:00+08', '2025-01-03 16:10:00+08'),
('ORD056', 'UID028', '手机壳透明防摔', 3, 59.70, 1, NULL, NULL, NULL, NULL, NULL),
('ORD057', 'UID029', '运动手环心率监测', 1, 169.00, 2, '中通快递', 'ZT1000000057', '运输中', '2025-01-05 10:30:00+08', NULL),
('ORD058', 'UID029', '手环替换腕带', 2, 39.80, 3, '圆通速递', 'YT1000000058', '已送达', '2025-01-01 14:50:00+08', '2025-01-04 09:30:00+08'),
('ORD059', 'UID030', '家用吸尘器大吸力', 1, 599.00, 3, '顺丰速运', 'SF1000000059', '已送达', '2024-12-31 09:10:00+08', '2025-01-03 13:15:00+08'),
('ORD060', 'UID030', '吸尘器滤芯套装', 1, 89.00, 1, NULL, NULL, NULL, NULL, NULL),

-- 订单61-80
('ORD061', 'UID031', '无线麦克风领夹式', 1, 249.00, 3, '顺丰速运', 'SF1000000061', '已送达', '2025-01-02 11:20:00+08', '2025-01-03 15:45:00+08'),
('ORD062', 'UID031', '麦克风收纳盒', 1, 29.00, 2, '京东物流', 'JD1000000062', '运输中', '2025-01-04 13:10:00+08', NULL),
('ORD063', 'UID032', '电烤箱30L大容量', 1, 459.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD064', 'UID032', '烤箱烘焙油纸', 3, 29.70, 3, '中通快递', 'ZT1000000064', '已送达', '2025-01-01 10:25:00+08', '2025-01-03 11:50:00+08'),
('ORD065', 'UID033', '台式电脑主机i7', 1, 5699.00, 3, '顺丰速运', 'SF1000000065', '已送达', '2024-12-26 14:30:00+08', '2024-12-30 16:20:00+08'),
('ORD066', 'UID033', '主机机箱风扇', 3, 89.70, 3, '德邦快递', 'DP1000000066', '已送达', '2025-01-02 09:50:00+08', '2025-01-05 10:15:00+08'),
('ORD067', 'UID034', '空气炸锅5.5L', 1, 399.00, 0, NULL, NULL, NULL, NULL, NULL),
('ORD068', 'UID034', '空气炸锅专用纸', 2, 39.80, 3, '圆通速递', 'YT1000000068', '已送达', '2025-01-03 12:40:00+08', '2025-01-05 14:30:00+08'),
('ORD069', 'UID035', '电动滑板车代步', 1, 1599.00, 2, '顺丰速运', 'SF1000000069', '运输中', '2025-01-01 08:30:00+08', NULL),
('ORD070', 'UID035', '滑板车充电器', 1, 129.00, 3, '京东物流', 'JD1000000070', '已送达', '2025-01-02 15:15:00+08', '2025-01-04 11:20:00+08'),
('ORD071', 'UID036', '家用投影仪支架', 1, 159.00, 3, '中通快递', 'ZT1000000071', '已送达', '2025-01-01 13:45:00+08', '2025-01-03 16:35:00+08'),
('ORD072', 'UID036', '投影仪高清线', 1, 49.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD073', 'UID037', '颈椎按摩器颈部', 1, 299.00, 3, '顺丰速运', 'SF1000000073', '已送达', '2025-01-02 10:10:00+08', '2025-01-04 15:10:00+08'),
('ORD074', 'UID037', '按摩器舒缓凝胶贴', 4, 59.60, 4, NULL, NULL, NULL, NULL, NULL),
('ORD075', 'UID038', '路由器散热器', 1, 39.00, 3, '圆通速递', 'YT1000000075', '已送达', '2025-01-03 11:20:00+08', '2025-01-05 09:45:00+08'),
('ORD076', 'UID038', '网线水晶头10颗', 1, 12.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD077', 'UID039', '多功能螺丝刀套装', 1, 69.00, 2, '中通快递', 'ZT1000000077', '运输中', '2025-01-05 14:15:00+08', NULL),
('ORD078', 'UID039', '精密镊子套装', 2, 29.80, 3, '京东物流', 'JD1000000078', '已送达', '2025-01-01 12:30:00+08', '2025-01-02 14:15:00+08'),
('ORD079', 'UID040', '电子秤高精度厨房', 1, 59.00, 3, '顺丰速运', 'SF1000000079', '已送达', '2025-01-02 08:20:00+08', '2025-01-03 13:50:00+08'),
('ORD080', 'UID040', '厨房秤电池', 2, 9.90, 0, NULL, NULL, NULL, NULL, NULL),

-- 订单81-100
('ORD081', 'UID041', '游戏手柄无线版', 1, 199.00, 3, '顺丰速运', 'SF1000000081', '已送达', '2025-01-01 09:50:00+08', '2025-01-02 15:30:00+08'),
('ORD082', 'UID041', '手柄摇杆帽', 4, 29.60, 1, NULL, NULL, NULL, NULL, NULL),
('ORD083', 'UID042', '车载充电器快充', 1, 69.00, 3, '中通快递', 'ZT1000000083', '已送达', '2025-01-03 10:40:00+08', '2025-01-05 11:45:00+08'),
('ORD084', 'UID042', '车载手机支架吸盘', 1, 39.00, 2, '圆通速递', 'YT1000000084', '运输中', '2025-01-04 11:30:00+08', NULL),
('ORD085', 'UID043', '监控摄像头家用', 1, 249.00, 3, '顺丰速运', 'SF1000000085', '已送达', '2024-12-31 13:20:00+08', '2025-01-02 16:10:00+08'),
('ORD086', 'UID043', '摄像头内存卡64G', 1, 69.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD087', 'UID044', '暖风机办公室迷你', 1, 99.00, 3, '京东物流', 'JD1000000087', '已送达', '2025-01-01 14:10:00+08', '2025-01-03 10:50:00+08'),
('ORD088', 'UID044', '暖风机防尘罩', 1, 25.00, 0, NULL, NULL, NULL, NULL, NULL),
('ORD089', 'UID045', '破壁机家用静音', 1, 699.00, 2, '顺丰速运', 'SF1000000089', '运输中', '2025-01-02 13:30:00+08', NULL),
('ORD090', 'UID045', '破壁机清洁刷', 1, 19.00, 3, '中通快递', 'ZT1000000090', '已送达', '2025-01-03 09:15:00+08', '2025-01-06 12:20:00+08'),
('ORD091', 'UID046', '手机充电宝20000mAh', 1, 159.00, 3, '圆通速递', 'YT1000000091', '已送达', '2025-01-01 11:40:00+08', '2025-01-02 13:25:00+08'),
('ORD092', 'UID046', '充电宝快充线', 1, 29.00, 1, NULL, NULL, NULL, NULL, NULL),
('ORD093', 'UID047', '蓝牙耳机充电仓', 1, 89.00, 3, '顺丰速运', 'SF1000000093', '已送达', '2025-01-02 14:25:00+08', '2025-01-04 09:10:00+08'),
('ORD094', 'UID047', '耳机硅胶套', 3, 19.70, 4, NULL, NULL, NULL, NULL, NULL),
('ORD095', 'UID048', '指纹锁专用电池', 1, 29.00, 3, '京东物流', 'JD1000000095', '已送达', '2025-01-01 10:15:00+08', '2025-01-03 15:15:00+08'),
('ORD096', 'UID048', '锁芯润滑喷剂', 1, 15.00, 2, '中通快递', 'ZT1000000096', '运输中', '2025-01-05 08:20:00+08', NULL),
('ORD097', 'UID049', '桌面收纳盒多层', 1, 79.00, 3, '圆通速递', 'YT1000000097', '已送达', '2025-01-03 13:30:00+08', '2025-01-05 16:40:00+08'),
('ORD098', 'UID049', '收纳盒分隔片', 2, 19.80, 1, NULL, NULL, NULL, NULL, NULL),
('ORD099', 'UID050', 'LED台灯护眼学习', 1, 129.00, 3, '顺丰速运', 'SF1000000099', '已送达', '2025-01-01 15:10:00+08', '2025-01-02 11:30:00+08'),
('ORD100', 'UID050', '台灯适配器', 1, 49.00, 0, NULL, NULL, NULL, NULL, NULL);




INSERT INTO t_ticket (user_id, order_id, category, title, description, priority, status, assigned_to, resolved_at, closed_at)
VALUES
-- 1-10
('UID001', 'ORD001', '物流问题', '商品未按时送达', '购买的蓝牙耳机显示已送达但未收到，物流信息不更新', 2, 1, '客服张三', '2025-01-03 16:00:00+08', NULL),
('UID003', 'ORD005', '商品问题', '游戏鼠标按键失灵', '刚收到的游戏鼠标左键偶尔无响应，无法正常使用', 3, 2, '技术李四', '2025-01-06 10:30:00+08', '2025-01-06 12:00:00+08'),
('UID005', 'ORD009', '售后退款', '智能手表功能不符', '手表宣传的心率监测不准确，申请退货退款', 2, 3, '客服王五', '2025-01-03 11:20:00+08', '2025-01-03 15:10:00+08'),
('UID007', 'ORD013', '订单问题', '咖啡机未发货', '已支付3天，订单一直显示待发货，无人处理', 2, 0, NULL, NULL, NULL),
('UID009', 'ORD017', '物流问题', '投影仪运输损坏', '收到包裹发现外壳破裂，投影仪无法开机', 3, 1, '售后赵六', '2025-01-01 14:00:00+08', NULL),
('UID011', 'ORD021', '商品问题', '充电板无法充电', '无线充电板插上电源无反应，无法给手机充电', 1, 2, '客服张三', '2025-01-03 09:15:00+08', '2025-01-03 11:00:00+08'),
('UID013', 'ORD025', '售后换货', '蓝牙音箱杂音大', '播放音乐时有明显电流声，影响使用，申请换货', 2, 1, '技术李四', '2025-01-05 16:30:00+08', NULL),
('UID015', 'ORD029', '订单问题', '相机背包发错货', '购买的黑色背包收到的是蓝色，与订单不符', 1, 2, '客服王五', '2025-01-05 10:20:00+08', '2025-01-05 13:40:00+08'),
('UID017', 'ORD033', '使用问题', '路由器无法联网', '按照说明书设置后始终无法连接网络，需要技术支持', 2, 0, NULL, NULL, NULL),
('UID019', 'ORD037', '商品问题', '加湿器漏水', '使用一天后发现底部漏水，浸湿桌面', 2, 3, '售后赵六', '2025-01-06 09:00:00+08', '2025-01-06 14:20:00+08'),

-- 11-20
('UID021', 'ORD041', '售后维修', '筋膜枪无法启动', '使用两次后突然无法开机，充电也无反应', 3, 1, '客服张三', '2025-01-05 15:00:00+08', NULL),
('UID023', 'ORD045', '物流问题', '吹风机丢件', '物流信息停留在发货地，超过7天无更新', 2, 2, '技术李四', '2025-01-04 11:10:00+08', '2025-01-04 16:30:00+08'),
('UID025', 'ORD049', '商品问题', '蓝牙键盘按键失灵', '部分按键按下无反应，影响办公使用', 1, 1, '客服王五', '2025-01-02 14:30:00+08', NULL),
('UID027', 'ORD053', '售后退款', '料理锅加热异常', '加热速度极慢，达不到宣传效果，申请退款', 2, 3, '售后赵六', '2025-01-03 16:20:00+08', '2025-01-03 18:00:00+08'),
('UID029', 'ORD057', '订单问题', '运动手环发错型号', '购买的标准版收到的是Pro版，价格不符', 1, 0, NULL, NULL, NULL),
('UID031', 'ORD061', '使用问题', '麦克风连接失败', '无线麦克风无法与手机蓝牙连接，搜索不到设备', 2, 2, '客服张三', '2025-01-01 10:00:00+08', '2025-01-01 12:10:00+08'),
('UID033', 'ORD065', '商品问题', '电脑主机蓝屏', '新主机使用一天频繁蓝屏，无法正常运行', 3, 1, '技术李四', '2025-01-05 09:40:00+08', NULL),
('UID035', 'ORD069', '物流问题', '滑板车未收到', '订单显示已签收，但本人未签收，怀疑丢件', 3, 2, '客服王五', '2025-01-02 15:30:00+08', '2025-01-02 18:20:00+08'),
('UID037', 'ORD073', '售后换货', '颈椎按摩器力度不足', '按摩力度太小，达不到舒缓效果，申请换货', 1, 1, '售后赵六', '2025-01-04 13:00:00+08', NULL),
('UID039', 'ORD077', '订单问题', '螺丝刀套装缺少配件', '收到的工具套装少了2个批头，不完整', 1, 3, '客服张三', '2025-01-06 11:00:00+08', '2025-01-06 15:10:00+08'),

-- 21-30
('UID041', 'ORD081', '商品问题', '游戏手柄连接中断', '无线手柄使用时频繁断开连接，体验极差', 2, 0, NULL, NULL, NULL),
('UID043', 'ORD085', '使用问题', '监控摄像头离线', '设备经常自动离线，需要重新插拔电源才能恢复', 2, 1, '技术李四', '2025-01-03 14:20:00+08', NULL),
('UID045', 'ORD089', '售后维修', '破壁机噪音过大', '工作时噪音远超正常范围，影响使用', 2, 2, '客服王五', '2025-01-01 16:00:00+08', '2025-01-01 17:30:00+08'),
('UID047', 'ORD093', '物流问题', '耳机充电仓破损', '收到的商品包装破损，充电仓有划痕', 1, 1, '售后赵六', '2025-01-05 11:30:00+08', NULL),
('UID049', 'ORD097', '商品问题', '收纳盒质量问题', '塑料材质脆弱，组装时直接断裂', 1, 3, '客服张三', '2025-01-02 10:10:00+08', '2025-01-02 12:40:00+08'),
('UID002', 'ORD003', '订单取消', '订单误支付，申请取消', '不小心重复下单，需要取消其中一个订单并退款', 1, 2, '客服王五', '2025-01-01 09:00:00+08', '2025-01-01 11:20:00+08'),
('UID004', 'ORD007', '发票问题', '需要开具增值税发票', '购买显示器用于公司，需要开具专票', 1, 1, '财务刘七', '2025-01-03 15:00:00+08', NULL),
('UID006', 'ORD011', '物流催件', '净化器迟迟不发货', '已支付5天，订单仍未发货，催促尽快处理', 2, 0, NULL, NULL, NULL),
('UID008', 'ORD015', '订单修改', '修改收货地址', '下单地址填写错误，需要修改为正确地址', 1, 2, '客服张三', '2025-01-01 14:00:00+08', '2025-01-01 16:30:00+08'),
('UID010', 'ORD019', '售后咨询', '扫地机保修政策', '咨询产品保修年限和售后维修网点', 1, 3, '售后赵六', '2025-01-02 09:30:00+08', '2025-01-02 11:10:00+08'),

-- 31-40
('UID012', 'ORD023', '商品退货', '平板屏幕有亮点', '新机屏幕存在亮点，不符合质量标准，申请退货', 3, 1, '技术李四', '2025-01-04 10:00:00+08', NULL),
('UID014', 'ORD027', '安装问题', '智能门锁无法安装', '自行安装失败，需要上门安装服务', 2, 2, '安装孙八', '2025-01-05 16:00:00+08', '2025-01-05 18:10:00+08'),
('UID016', 'ORD031', '使用咨询', '打蛋器功能使用', '不清楚如何调节档位，需要使用指导', 1, 0, NULL, NULL, NULL),
('UID018', 'ORD035', '物流问题', '行车记录仪超时未送达', '预计3天到达，现已6天仍未收到', 2, 1, '客服王五', '2025-01-03 11:00:00+08', NULL),
('UID020', 'ORD039', '商品换货', '电热水壶不加热', '水壶通电但不加热，无法使用，申请换货', 2, 3, '售后赵六', '2025-01-01 15:30:00+08', '2025-01-01 17:40:00+08'),
('UID022', 'ORD043', '质量问题', '电磁炉不加热', '开机正常，但锅具始终不加热，无法使用', 3, 1, '技术李四', '2025-01-06 14:00:00+08', NULL),
('UID024', 'ORD047', '发票重开', '发票抬头错误', '之前开具的发票抬头有误，申请重开', 1, 2, '财务刘七', '2025-01-02 13:00:00+08', '2025-01-02 15:20:00+08'),
('UID026', 'ORD051', '售后咨询', '体脂秤连接问题', '无法连接手机APP，同步数据失败', 1, 1, '客服张三', '2025-01-01 12:00:00+08', NULL),
('UID028', 'ORD055', '物流签收', '非本人签收商品', '快递未经允许放在驿站，导致商品延误领取', 1, 3, '客服王五', '2025-01-03 10:00:00+08', '2025-01-03 12:30:00+08'),
('UID030', 'ORD059', '商品问题', '吸尘器吸力不足', '使用时吸力很小，达不到宣传效果', 2, 0, NULL, NULL, NULL);



INSERT INTO admin (user_id, username, phone_number, email, password, role, status)
VALUES
-- 1. 超级管理员（启用）
('ADM001', 'admin', '13999990001', 'admin@shop.com', 'e10adc3949ba59abbe56e057f20f883e', 'admin', 1),

-- 2. 客服管理员（启用）
('ADM002', 'admin_service', '13999990002', 'service@shop.com', 'e10adc3949ba59abbe56e057f20f883e', 'admin', 1),

-- 3. 订单管理员（启用）
('ADM003', 'admin_order', '13999990003', 'order@shop.com', 'e10adc3949ba59abbe56e057f20f883e', 'admin', 1),

-- 4. 工单管理员（禁用）
('ADM004', 'admin_ticket', '13999990004', 'ticket@shop.com', 'e10adc3949ba59abbe56e057f20f883e', 'admin', 0),

-- 5. 财务管理员（禁用）
('ADM005', 'admin_finance', '13999990005', 'finance@shop.com', 'e10adc3949ba59abbe56e057f20f883e', 'admin', 0);