package com.batch.zSpringBootBatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication //@Configuration + @EvableAutoConfiguration + @ComponentScan 이부분이 포함됨
//(exclude = { DataSourceAutoConfiguration.class }) //boot batch를 사용하려면 DataSource 설정이 필요하다. 사용하지 않으려면 해당 문구를 추가한다.
@PropertySource({"classpath:batch.properties"})
public class App {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(App.class, args);
	}
}
