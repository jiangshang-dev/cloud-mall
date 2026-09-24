package com.mall.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 扫描业务 Mapper（jeecg 默认只扫 org.jeecg.**.mapper*）
 */
@Configuration
@MapperScan(basePackages = {"com.mall"})
public class MallMybatisConfig {
}
