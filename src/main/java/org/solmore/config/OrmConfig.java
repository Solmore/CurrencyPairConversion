package org.solmore.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@Import(DatabaseConfig.class)
@PropertySource(value = "classpath:application.yaml",factory = YamlPropertySourceFactory.class)
public class OrmConfig {

    @Value("${spring.liquibase.change-log}") private String changeLog;
    @Value("${spring.liquibase.default-schema}") private String schema;
    @Bean("dataSource")
    DataSource dataSource(DatabaseConfig databaseConfig) {
        HikariConfig dataSourceProperties = new HikariConfig();
        dataSourceProperties.setJdbcUrl(databaseConfig.getJdbcUrl());
        dataSourceProperties.setUsername(databaseConfig.getUser());
        dataSourceProperties.setPassword(databaseConfig.getPassword());
        return new HikariDataSource(dataSourceProperties);
    }

    @Bean("entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                       DatabaseConfig databaseConfig) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(Database.POSTGRESQL);
        vendorAdapter.setShowSql(true);
        vendorAdapter.setGenerateDdl(true);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(databaseConfig.getPackages());
        factory.setDataSource(dataSource);
        factory.setPersistenceUnitName("defaultPersistenceUnit");
        factory.setJpaPropertyMap(Map.of(
                "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
                "hibernate.hbm2ddl.auto", "create-drop"
        ));
        factory.setEntityManagerFactoryInterface(EntityManagerFactory.class);
        return factory;
    }

    @Bean("liquibase")
    public SpringLiquibase liquibase(DatabaseConfig databaseConfig) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(changeLog);
        liquibase.setDataSource(dataSource(databaseConfig));
        liquibase.setDefaultSchema(schema);
        return liquibase;
    }

    @Bean("transactionManager")
    protected PlatformTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
