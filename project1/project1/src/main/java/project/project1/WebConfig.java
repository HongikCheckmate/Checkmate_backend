package project.project1;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/certification/**")
                .addResourceLocations("file:///C:/Users/Public/degree_project/certification/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                .allowedOrigins("http://localhost:5173") // 프론트엔드 출처
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // ★★★ OPTIONS를 반드시 포함 ★★★
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true);
    }
}
