package com.mall.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 扫描业务 Mapper。
 * 迁移后业务包仍为 org.jeecg.modules.*；同时保留 com.mall 扫描。
 */
@Configuration
@MapperScan(value = {
        "com.mall.**.mapper*",
        "org.jeecg.modules.**.mapper*"
})
public class MallMybatisConfig {
}
