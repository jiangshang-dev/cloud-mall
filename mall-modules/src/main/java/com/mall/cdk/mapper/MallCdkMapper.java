package com.mall.cdk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.cdk.entity.MallCdk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MallCdkMapper extends BaseMapper<MallCdk> {

    /**
     * 原子占坑：仅 UNUSED 且未过期才能核销成功（影响行数=1）
     */
    @Update("UPDATE mall_cdk SET status = 'USED', used_by = #{userId}, used_time = NOW(), update_time = NOW() " +
            "WHERE code = #{code} AND status = 'UNUSED' AND (expire_time IS NULL OR expire_time > NOW())")
    int occupy(@Param("code") String code, @Param("userId") String userId);
}
