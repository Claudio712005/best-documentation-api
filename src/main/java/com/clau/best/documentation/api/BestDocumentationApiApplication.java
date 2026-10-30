package com.clau.best.documentation.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BestDocumentationApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BestDocumentationApiApplication.class, args);
    }

}
