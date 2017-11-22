package com.batch.zSpringBootBatch.ex;

import java.util.List;

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
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

//@Configuration			//기본 설정 선언
//@EnableBatchProcessing 	//기본 설정 선언
//@EnableScheduling  		//스케줄러 사용 선언
public class Step1Reader1Writer2 { //http://www.javainuse.com/spring/bootbatch 참고


	private static final String BATCH_NAME = "Launcher1Job1Step2";

	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private SimpleJobLauncher jobLauncher;

	/**
	 * 스케쥴러
	 * 
	 * 주기적으로 Job 실행
	 * cron 설정에 따라 실행
	 *  jobLauncher 1개당 job 1+ 와 step 2 를 사용
	 *  sampleItemProcessor1~2를 만들어 사용
	 * 	sampleItemReader1개 사용
	 * 	sampleItemWriter1~2개 사용
	 * 
	 * 
	 * @throws Exception
	 */
//	@Scheduled(cron = "${scheduler.cron}") //사용시 주석을 풀자
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
		
		ItemReader<String> reader =  sampleItemReader();
		return jobBuilderFactory.get("[Job - " + BATCH_NAME + "]")
				.incrementer(new RunIdIncrementer())
				.flow(baseStep1(reader))
				.next(baseStep2(reader))
				.end()
				.build();
	}

	/**
	 * 배치 Step
	 * 
	 * <pre>
	 * reader() : 더미 데이터 생성
	 * writer() : sysout 찍기
	 * </pre>
	 * 
	 * chunk 단위로 실행 reader 로 읽고 processor 에서 사용자 정의데로 수정하며 writer로 데이터 처리를 합니다.
	 * @return
	 */
	@Bean
	public Step baseStep1(ItemReader<String> reader) { //chunk 큰덩어리 프로세스단위
		return stepBuilderFactory.get("[baseStep1 - " + BATCH_NAME + "]")
				.<String, String>chunk(20).reader(reader)
				.processor(sampleItemProcessor1())
				.writer(sampleItemWriter1())
				.build();
	}
	
	@Bean
	public Step baseStep2(ItemReader<String> reader) { //chunk 큰덩어리 프로세스단위
		return stepBuilderFactory.get("[baseStep2 - " + BATCH_NAME + "]")
				.<String, String>chunk(20).reader(reader)
				.processor(sampleItemProcessor2())
				.writer(sampleItemWriter2())
				.build();
	}

	 @Bean
	    public ItemReader<String> sampleItemReader() {
		
		 
		 ItemReader<String> reader = new ItemReader<String>() {
			 String[] messages = { "sample data",
						"Welcome to Spring Batch Example",
						"Database for this example" };
			 int count = 0;
			@Override
			public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
				if (count < messages.length) {
					return messages[count++];
				} else {
					count = 0;
				}
				return null;
			}
		};
		

	        return reader;
	    }

	    @Bean
	    public ItemProcessor<String, String> sampleItemProcessor1() {
	        return new ItemProcessor<String, String>() {
				
				@Override
				public String process(String data) throws Exception {
					return data.toUpperCase();
				}
			};
	    }
	    
	    @Bean
	    public ItemProcessor<String, String> sampleItemProcessor2() {
	        return new ItemProcessor<String, String>() {
				
				@Override
				public String process(String data) throws Exception {
					if(data.endsWith("data")) {
						return data.toUpperCase();
					}else {
						return null;
					}
					
				}
			};
	    }

	    @Bean
	    public ItemWriter<String> sampleItemWriter1() {
	    	ItemWriter<String> writer = new ItemWriter<String>() {
				
				@Override
				public void write(List<? extends String> items) throws Exception {
					for (String msg : items) {
						
							System.out.println(msg);
						
					}
					
				}
			};

	        return writer;
	    }
	    
	    @Bean
	    public ItemWriter<String> sampleItemWriter2() {
	    	ItemWriter<String> writer = new ItemWriter<String>() {
				
				@Override
				public void write(List<? extends String> items) throws Exception {
					for (String msg : items) {
						System.out.println("the data " + msg);
					}
					
				}
			};

	        return writer;
	    }

}
