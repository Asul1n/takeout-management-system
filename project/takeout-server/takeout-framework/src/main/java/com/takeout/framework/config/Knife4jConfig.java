package com.takeout.framework.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / Swagger API 文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("外卖管理系统 API")
                        .version("1.0.0")
                        .description("外卖管理系统 RESTful API 接口文档")
                        .contact(new Contact()
                                .name("Takeout Team")
                                .email("team@takeout.com")));
    }
}
