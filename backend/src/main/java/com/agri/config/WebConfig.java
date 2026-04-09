package com.agri.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，配置CORS等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir:D:/agri-platform/upload/}")
    private String imageUploadPath;
    
    /**
     * 配置CORS跨域支持
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8082")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以 / 结尾
        String imagePath = imageUploadPath;
        if (!imagePath.endsWith("/")) {
            imagePath = imagePath + "/";
        }
        
        // 将 /images/** 映射到实际的图片存储路径
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:/" + imagePath);
        
        // 添加头像文件访问映射 - 使用与上传相同的目录配置
        String uploadDir = imagePath;  // 使用与上传配置相同的路径
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:/" + uploadDir);
    }

    /**
     * 配置视图控制器，将所有未匹配的路径转发到index.html
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/{x:[\\w\\-]+}").setViewName("forward:/index.html");
        registry.addViewController("/{x:^(?!api$).*$}/**/{y:[\\w\\-]+}").setViewName("forward:/index.html");
    }
}
