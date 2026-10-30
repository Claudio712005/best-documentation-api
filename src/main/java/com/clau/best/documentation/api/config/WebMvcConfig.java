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

/**
 * Makes the outcome of locale negotiation visible on the wire.
 */
@Configuration(proxyBeanMethods = false)
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new NegotiatedLanguageInterceptor());
    }

    /**
     * Echoes {@code Content-Language} on every response.
     *
     * <p>Required by RFC 9110 whenever a representation is language-specific, and load-bearing in
     * practice: without it a shared cache cannot tell a Portuguese response from an English one at
     * the same URL, and a client has no way to notice that the language it asked for was not served.
     *
     * <p>Set in {@code preHandle} rather than {@code postHandle} because the locale is resolved
     * before the handler runs and the response may already be committed afterwards.
     */
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
