package com.clau.best.documentation.api.config;

import com.clau.best.documentation.api.shared.i18n.SupportedLanguage;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.nio.charset.StandardCharsets;

@Configuration(proxyBeanMethods = false)
public class LocalizationConfig {

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(SupportedLanguage.locales());
        resolver.setDefaultLocale(SupportedLanguage.DEFAULT.locale());
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:i18n/messages",
                "classpath:i18n/validation",
                "classpath:i18n/openapi");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(SupportedLanguage.DEFAULT.locale());
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean defaultValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
