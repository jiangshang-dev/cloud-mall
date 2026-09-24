package com.mall.cdk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.cdk.entity.MallCdk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MallCdkMapper extends BaseMapper<MallCdk> {

    /** 售卖发货：UNUSED → RESERVED */
    @Update("UPDATE mall_cdk SET status = 'RESERVED', used_by = #{userId}, update_time = NOW() " +
            "WHERE id = #{id} AND status = 'UNUSED' AND (expire_time IS NULL OR expire_time > NOW())")
    int reserveById(@Param("id") String id, @Param("userId") String userId);

    /** 激活核销：UNUSED/RESERVED → USED */
    @Update("UPDATE mall_cdk SET status = 'USED', used_by = #{userId}, used_time = NOW(), update_time = NOW() " +
            "WHERE code = #{code} AND status IN ('UNUSED','RESERVED') AND (expire_time IS NULL OR expire_time > NOW())")
    int occupy(@Param("code") String code, @Param("userId") String userId);
}
