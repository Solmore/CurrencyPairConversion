package org.solmore.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Getter
@Setter
@PropertySource(value = "classpath:application.yaml",factory = YamlPropertySourceFactory.class)
public class DatabaseConfig {

    @Value("${spring.datasource.url}")      private String jdbcUrl;
    @Value("${spring.datasource.username}")     private String user;
    @Value("${spring.datasource.password}") private String password;
    @Value("${spring.datasource.packages}")      private String packages;
}
