package com.batch.zSpringBootBatch.ex;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.PassThroughFieldExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration			//기본 설정 선언
@EnableBatchProcessing 	//기본 설정 선언
@EnableScheduling  		//스케줄러 사용 선언
public class FtpTaskletStep2 { 

	private static final Logger logger = LoggerFactory.getLogger(FtpTaskletStep2.class);
	private static final Logger FILE_LOGGER = LoggerFactory.getLogger("FILE_LOG");
	private static final String BATCH_NAME = "FtpTaskletStep2";
	
	@Value("${batch.file.path}")
	private String FILE_PATH;
	@Value("${batch.file.prefix}")
	private String FILE_PREFIX;

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
	 *  DB 접근 후 SELECT 및 파일 만들고 FTP 전송
	 *  
	 * @throws Exception
	 */
	@Scheduled(cron = "${scheduler.cron}") //properties 파일에 설정을 읽음
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
	@Scope("prototype")
	public Job baseJob() {
		return jobBuilderFactory.get("[Job - " + BATCH_NAME + "]").incrementer(new RunIdIncrementer())
				.flow(baseStep()).next(ftpStep()).end().build();
	}

	/**
	 * 배치 Step
	 * 
	 * <pre>
	 * reader() : SELECT하기
	 * writer() : 파일 만들기
	 * </pre>
	 * 
	 * 
	 * @return
	 */
	@Bean
	@Scope("prototype")
	public Step baseStep() { //chunk 큰덩어리 프로세스단위
		return stepBuilderFactory.get("[Step - " + BATCH_NAME + "]")
				.<Map<Integer, Object>, Map<Integer, Object>>chunk(20).reader(sampleItemReader()).writer(sampleItemWriter())
				.build();
	}
	

	/**
	 * 배치 Step
	 * 
	 * <pre>
	 * ftp전송
	 * </pre>
	 * 
	 * 
	 * @return
	 */
	@Bean
	
	public Step ftpStep() { //chunk 큰덩어리 프로세스단위
		return  stepBuilderFactory.get("[ftpStep - " + BATCH_NAME + "]").tasklet(tasklet()).build();
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
	    @Scope("prototype")
	    public ItemWriter<Map<Integer, Object>> sampleItemWriter() {
	    	System.out.println("ItemWriter Started at :" + new Date());

			String dateFormat = DateFormatUtils.format(new Date(), "yyyyMMdd");
			String exportFilePath = FILE_PATH + FILE_PREFIX + dateFormat + ".txt";
			
			FlatFileItemWriter<Map<Integer, Object>> writer = new FlatFileItemWriter<Map<Integer, Object>>();
			writer.setEncoding("EUC-KR");
			writer.setResource(new FileSystemResource(exportFilePath));
			writer.setLineAggregator(new DelimitedLineAggregator<Map<Integer, Object>>() {
				{
					setDelimiter("|");
					setFieldExtractor(new PassThroughFieldExtractor<Map<Integer, Object>>());
				}
			});
			
			
			writer.setFooterCallback(new FlatFileFooterCallback() {
				
				@Override
				public void writeFooter(Writer writer) throws IOException {
					PrintWriter pw = new PrintWriter(writer);                
		            pw.println("end") ;
		            pw.flush();	
				}
			});
		
	        return writer;
	    }
	    

		@Bean
		public FtpTasklet tasklet() {
			FtpTasklet tasklet = new FtpTasklet();
			return tasklet;
		}
		
}
