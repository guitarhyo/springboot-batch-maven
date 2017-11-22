package com.batch.zSpringBootBatch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

/**
 *  DB 설정
 * 
 *
 */
@Configuration
@PropertySource({ "classpath:database.properties" }) //database.properties 주입 시킨다.
public class DatabaseConfig {

	@Bean
	@Primary
	@ConfigurationProperties("datasource.mysql")  //datasource.mssql prefix로 주입 한다. DataSourceProperties 안에 코드를 보면 알 수있음
	public DataSourceProperties fooDataSourceProperties() {
	    return new DataSourceProperties();
	}

	@Bean
	@Primary
	@ConfigurationProperties("datasource.mysql") //DataSource에 프로퍼티 주입
	public DataSource dataSource() {
	    return fooDataSourceProperties().initializeDataSourceBuilder().build();
	}

	
	@Bean  //BatchConfig 에 datasource 주입
	public BatchConfigurer configurer(DataSource dataSource) {
	    return new DefaultBatchConfigurer(dataSource);
	}
}
