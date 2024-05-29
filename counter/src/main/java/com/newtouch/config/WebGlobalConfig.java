package com.newtouch.config;

import jakarta.annotation.Resource;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;

/** <p>SpringMvc配置<p>
 * <li>自定义Spring MVC的特性和扩展Spring MVC的功能</li>
 * @since 2023/9/21
 * @author CC
 **/
@SpringBootConfiguration
public class WebGlobalConfig implements WebMvcConfigurer {

    @Resource
    private MyHandlerInterceptor myHandlerInterceptor;

    /**
     * 拦截器（Interceptors）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        ArrayList<String> list = new ArrayList<>();
        list.add("/login");
        //自定义拦截器，或其他拦截器
        registry.addInterceptor(myHandlerInterceptor)
                //添加拦截地址为所有拦截
                .addPathPatterns("/**")
                //不拦截的地址
                .excludePathPatterns(list);
    }

    /**
     * 资源处理器（Resource Handlers）
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/resources/","classpath:/static/");
    }

    /**
     * 跨域资源共享（CORS）
     */
    @Bean
    public CorsFilter corsFilter() {
        //创建CorsConfiguration对象后添加配置
        CorsConfiguration config = new CorsConfiguration();
        //允许所有原始域
        config.addAllowedOriginPattern("*");
        //允许所有头部信息
        config.addAllowedHeader("*");
        //允许所有头部信息
        config.addExposedHeader("*");
        //放行的请求方式
//        config.addAllowedMethod("GET");
//        config.addAllowedMethod("PUT");
//        config.addAllowedMethod("POST");
//        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("*");     //放行全部请求

        //是否发送Cookie
        config.setAllowCredentials(true);

        //2. 添加映射路径
        UrlBasedCorsConfigurationSource corsConfigurationSource =
                new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", config);
        //返回CorsFilter
        return new CorsFilter(corsConfigurationSource);
    }
}
