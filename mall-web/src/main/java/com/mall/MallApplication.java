package com.mall;

import com.xkcoding.justauth.autoconfigure.JustAuthAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * cloud-mall 单体启动类（替代 jeecg-system-start）。
 * <p>
 * 底层：本地 Maven 中的 jeecg-boot 3.9.5（jeecg-system-biz）；
 * 业务：com.mall.*；端口 8080、上下文 /jeecg-boot，与 jeecgboot-vue3 默认代理一致。
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"org.jeecg", "com.mall"})
@ImportAutoConfiguration(JustAuthAutoConfiguration.class)
public class MallApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(MallApplication.class);
    }

    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(MallApplication.class);
        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("management.health.elasticsearch.enabled", false);
        app.setDefaultProperties(defaultProperties);

        ConfigurableApplicationContext application = app.run(args);
        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = oConvertUtils.getString(env.getProperty("server.servlet.context-path"));
        log.info("\n----------------------------------------------------------\n\t" +
                "Application cloud-mall is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/doc.html\n\t" +
                "Swagger文档: \thttp://" + ip + ":" + port + path + "/doc.html\n" +
                "----------------------------------------------------------");
    }
}
