package com.storm.config;

import com.storm.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加JWT拦截器，应用到需要验证的路径
        registry.addInterceptor(jwtInterceptor)
                //必须手动保护,不然是不解析令牌,取到的值是null
                .addPathPatterns("/api/user/**", "/api/admin/**","/api/ai/**","/api/history/**","/api/store/**") // 应用到user相关的所有路径
                .excludePathPatterns("/api/auth/login", "/api/user/customer/register","/api/public/**"); // 放行路径
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有路径生效
                .allowedOriginPatterns("http://localhost:5173","http://localhost:5174","http://localhost") // 允许来自Vite开发服务器的请求
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true); // 允许携带凭据（如cookies、authorization headers等）
    }
}
