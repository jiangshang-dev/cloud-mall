package com.mall.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(value = {"com.mall.**.mapper*"})
public class MallMybatisConfig {
}