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

/**
 * Wires locale negotiation and message resolution.
 *
 * <p>Three distinct things are localized in this API, and they are deliberately kept separate:
 *
 * <ol>
 *   <li><b>The OpenAPI document</b> — summaries, descriptions and tag names, resolved from
 *       {@code i18n/openapi*.properties} (see {@link OpenApiConfig}).
 *   <li><b>Runtime responses</b> — error titles, details and validation violations, resolved from
 *       {@code i18n/messages*.properties}.
 *   <li><b>Domain content</b> — product names and the like, which are data rather than copy and
 *       therefore live with the resources themselves, not in a bundle.
 * </ol>
 */
@Configuration(proxyBeanMethods = false)
public class LocalizationConfig {

    /**
     * Negotiates the locale straight from {@code Accept-Language}.
     *
     * <p>No cookie or session resolver on purpose: a REST API has no session, so the header is the
     * single source of truth and the same request always yields the same representation.
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(SupportedLanguage.locales());
        resolver.setDefaultLocale(SupportedLanguage.DEFAULT.locale());
        return resolver;
    }

    /** Bundle for everything the client reads at runtime: problem details and violations. */
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

    /**
     * Replaces the auto-configured validator so that {@code jakarta.validation} messages are read
     * from {@link #messageSource()} instead of Hibernate Validator's own bundle.
     *
     * <p>Without this, a {@code @NotBlank} violation would always answer in English while the rest
     * of the payload honours {@code Accept-Language}.
     */
    @Bean
    public LocalValidatorFactoryBean defaultValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }
}
