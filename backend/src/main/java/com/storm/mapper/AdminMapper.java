package com.storm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.storm.entity.Admin;
import com.storm.entity.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 统计用户消费金额分布
     * 返回结果示例: [{level: '低消费', count: 10}, {level: '高消费', count: 5}]
     */
    @MapKey("level_name")
    List<Map<String, Object>> selectUserSpendingStats();

    /**
     * 统计快递公司占比
     */
    @MapKey("company_name")
    List<Map<String, Object>> selectCourierStats();

    // 新增方法（用于 VIP 和工单类别统计）
    @MapKey("vip_level")
    List<Map<String, Object>> selectVipLevelStats();

    @MapKey("category")
    List<Map<String, Object>> selectTicketCategoryStats();

    /**
     * 统计积分区间分布
     */
    @MapKey("level_name")
    List<Map<String, Object>> selectUserPointsStats();
}
