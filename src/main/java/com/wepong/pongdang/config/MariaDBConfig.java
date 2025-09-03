package com.wepong.pongdang.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.wepong.pongdang.repository",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class MariaDBConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public org.springframework.boot.autoconfigure.jdbc.DataSourceProperties primaryDsProps() {
        return new org.springframework.boot.autoconfigure.jdbc.DataSourceProperties();
    }

    @Primary
    @Bean(name = "primaryDataSource")
    public javax.sql.DataSource primaryDataSource(
            org.springframework.boot.autoconfigure.jdbc.DataSourceProperties props) {
        // props.getUrl(), getUsername(), getPassword()를 이용해 올바르게 생성해 줍니다.
        return props.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource ds) {

        return builder
                .dataSource(ds)
                .packages("com.wepong.pongdang")   // 메인 엔티티 패키지
                .persistenceUnit("primary")
                .build();
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
