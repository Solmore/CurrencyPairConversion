package org.solmore.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@Configuration
@ComponentScan("org.solmore")
@EnableJpaRepositories("org.solmore.repository")
@Import({OrmConfig.class, RedisConfig.class})
public class CurrencyConfiguration {

}
