package com.nowcoder.community.config;

import com.nowcoder.community.controller.inteceptor.DataInterceptor;
import com.nowcoder.community.controller.inteceptor.LoginRequiredInterceptor;
import com.nowcoder.community.controller.inteceptor.LoginTicketInterceptor;
import com.nowcoder.community.controller.inteceptor.MessageInterceptor;
import com.nowcoder.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

//    @Autowired
//    LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor).
                excludePathPatterns("/**/*.css", "/**/*.jss", "/**/*.jpg", "**/*.png", "**/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor).
//                excludePathPatterns("/**/*.css", "/**/*.jss", "/**/*.jpg", "**/*.png", "**/*.jpeg");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
