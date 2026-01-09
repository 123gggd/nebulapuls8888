package com.nebula.commerce.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 读取与 FileController 一致的路径配置
    @Value("${nebula.upload.path:${user.dir}/uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以文件分隔符结尾
        if (!uploadPath.endsWith(File.separator)) {
            uploadPath += File.separator;
        }

        // 建立映射：/images/** ==>  file:./uploads/
        // 注意：addResourceLocations 必须以 "file:" 开头
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath);
    }

    /**
     * 补充全局 CORS 配置，防止 Security 链之外的请求跨域问题
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}