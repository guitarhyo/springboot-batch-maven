package com.batch.zSpringBootBatch.ex;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

//@Configuration			//기본 설정 선언
//@EnableBatchProcessing 	//기본 설정 선언
//@EnableScheduling  		//스케줄러 사용 선언
public class DatabaseReader1Writer1 { //http://www.javainuse.com/spring/bootbatch 참고


	private static final String BATCH_NAME = "DatabaseReader1Writer1";

	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private SimpleJobLauncher jobLauncher;
	
	@Autowired //DB 설정에 따른 주입
	private DataSource dataSource; 

	/**
	 * 스케쥴러
	 * 
	 * 주기적으로 Job 실행
	 * cron 설정에 따라 실행
	 *  DB 접근 후 SELECT 및 INSERT 사용
	 *  
	 * @throws Exception
	 */
//	@Scheduled(cron = "${scheduler.cron}") //properties 파일에 설정을 읽음
	public void scheduler() throws Exception {
		String jobId = String.valueOf(System.currentTimeMillis());
	
		System.out.println("Started jobId : "+ jobId);

		JobParameters param = new JobParametersBuilder().addString("JobID", jobId).toJobParameters();
		JobExecution execution = jobLauncher.run(baseJob(), param);

		System.out.println("end : " + param.getString("JobID") +":::"+ execution.getStatus());
	}

	/**
	 * 배치 Job
	 * 
	 * baseStep 호출한다
	 *  
	 * @return
	 */
	@Bean
	public Job baseJob() {
		return jobBuilderFactory.get("[Job - " + BATCH_NAME + "]").incrementer(new RunIdIncrementer())
				.start(baseStep()).build();
	}

	/**
	 * 배치 Step
	 * 
	 * <pre>
	 * reader() : SELECT하기
	 * writer() : INSERT하기
	 * </pre>
	 * 
	 * 
	 * @return
	 */
	@Bean
	public Step baseStep() { //chunk 큰덩어리 프로세스단위
		return stepBuilderFactory.get("[Step - " + BATCH_NAME + "]")
				.<Map<Integer, Object>, Map<Integer, Object>>chunk(20).reader(sampleItemReader()).processor(sampleItemProcessor()).writer(sampleItemWriter())
				.build();
	}

	 @Bean
	    public ItemReader<Map<Integer, Object>> sampleItemReader() {
		 JdbcCursorItemReader<Map<Integer, Object>> reader = new JdbcCursorItemReader<Map<Integer, Object>>();
			reader.setDataSource(dataSource);
		 

			
			reader.setSql("select NO,NAME,PHONE_NO FROM new_test");
			reader.setRowMapper(new RowMapper<Map<Integer, Object>>() {
				@Override
				public Map<Integer, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
					if (!(rs.isAfterLast()) && !(rs.isBeforeFirst())) {
						Map<Integer, Object> map = new HashMap<Integer, Object>();
						
						map.put(1, rs.getString("NO"));
						map.put(2, rs.getString("NAME"));
						map.put(3, rs.getString("PHONE_NO"));
						
						

						System.out.println("RowMapper record : {}"+ map);
						return map;
					} else {
						System.out.println("Returning null from rowMapper");
						return null;
					}
				}
			});


	        return reader;
	    }

	    @Bean
	    public ItemProcessor<Map<Integer, Object>,Map<Integer, Object>> sampleItemProcessor() {
	        return new ItemProcessor<Map<Integer,Object>, Map<Integer,Object>>() {
				
				@Override
				public Map<Integer, Object> process(Map<Integer, Object> item) throws Exception {
					
					String tmp = (String) item.get(2);
					if("홍길동".equals(tmp)) {
						item.put(3, "empty");
						return item;
					}
					return null;
				}
			};
	    }

	    @Bean
	    public ItemWriter<Map<Integer, Object>> sampleItemWriter() {
	    	JdbcBatchItemWriter<Map<Integer, Object>> writer = new JdbcBatchItemWriter<>();
			writer.setItemSqlParameterSourceProvider(
					new BeanPropertyItemSqlParameterSourceProvider<Map<Integer, Object>>());
			writer.setItemPreparedStatementSetter(insertSetter());

			writer.setSql(
					"insert into new_test (NAME,PHONE_NO) values (?,?)");

			writer.setDataSource(dataSource);
	        return writer;
	    }
	    
	    @Bean
		public ItemPreparedStatementSetter<Map<Integer, Object>> insertSetter() {
			return new ItemPreparedStatementSetter<Map<Integer, Object>>() {
				@Override
				public void setValues(Map<Integer, Object> item, PreparedStatement ps) throws SQLException {
					
					ps.setString(1, (String) item.get(2));
					ps.setString(2, (String) item.get(3));
				}
			};
		}

}
