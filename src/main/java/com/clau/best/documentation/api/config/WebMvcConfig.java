package com.clau.best.documentation.api.config;

import com.clau.best.documentation.api.shared.api.ApiHeaders;
import com.clau.best.documentation.api.shared.i18n.SupportedLanguage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new NegotiatedLanguageInterceptor());
    }

    static class NegotiatedLanguageInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(
                HttpServletRequest request, HttpServletResponse response, Object handler) {

            response.setHeader(
                    ApiHeaders.CONTENT_LANGUAGE,
                    SupportedLanguage.closestTo(LocaleContextHolder.getLocale()).tag());
            return true;
        }
    }
}
