package com.batch.zSpringBootBatch.ex;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class RestTemplateEx1 {
	private JdbcTemplate jdbcTemplate;

	@Autowired
	public RestTemplateEx1(DataSource dataSource) {
	    this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Scheduled(cron = "${scheduler.cron}")
	public void scheduler() throws Exception {
		// 인터셉터를 사용할경우~
		//		RestTemplate restTemplate = new RestTemplateBuilder().additionalInterceptors(
		//      Collections.singletonList(new LoggingInterceptor())).build();
			RestTemplate restTemplate = new RestTemplate();
			
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			
			requestFactory.setConnectTimeout(7000); // set short connect timeout
			requestFactory.setReadTimeout(7000); // set slightly longer read timeout
			restTemplate.setRequestFactory(requestFactory);
			
			
			ObjectNode res = restTemplate.postForObject("url", "", ObjectNode.class);
			System.out.println(res.toString());
			String code =res.get("code").textValue();
	}

	
	public List<Map<String,String>> selectList() throws Exception{
 	    List<Map<String,String>> results = jdbcTemplate.query("SQL",
 	            new RowMapper<Map<String,String>>() {
 	  
 	                @Override
 	                public Map<String,String> mapRow(ResultSet rs, int rowNum)
 	                    throws SQLException {
 	                	Map<String,String> map = new HashMap<String, String>();
 	                	map.put("serverName", rs.getString("serverName"));
 	                	map.put("comment", rs.getString("comment"));
 	                	map.put("url", rs.getString("url"));

 	                    return map;
 	                }
 	            });
 	    return results;
 	}
	
	public void sendSQL(final String code,final String message) throws Exception{

 	    List<SqlParameter> parameters = Arrays.asList(
 	            new SqlParameter(Types.VARCHAR), new SqlParameter(Types.VARCHAR));

 	    Map<String, Object> t = jdbcTemplate.call(new CallableStatementCreator() {

			@Override
			public CallableStatement createCallableStatement(Connection con)
					throws SQLException {
				  	CallableStatement callableStatement = con.prepareCall("SQL");
	 	            callableStatement.setString(1, code);
	 	            callableStatement.setString(2,message);
	 	            return callableStatement;
			}
 	      
 	    }, parameters);

 	}
	
	public static void main(String[] args) {
		
		// 인터셉터를 사용할경우~
		//		RestTemplate restTemplate = new RestTemplateBuilder().additionalInterceptors(
		//      Collections.singletonList(new LoggingInterceptor())).build();
			RestTemplate restTemplate = new RestTemplate();
			
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			
			requestFactory.setConnectTimeout(7000); // set short connect timeout
			requestFactory.setReadTimeout(7000); // set slightly longer read timeout
			restTemplate.setRequestFactory(requestFactory);
			
			 ObjectMapper objectMapper = new ObjectMapper();
			 ObjectNode node = objectMapper.createObjectNode();
			 node.put("testparam", "param1");
			ObjectNode res = restTemplate.postForObject("http://localhost:8080/test/json",node, ObjectNode.class);
			System.out.println(res.toString());
//			String code =res.get("code").textValue();
	}
    
}
