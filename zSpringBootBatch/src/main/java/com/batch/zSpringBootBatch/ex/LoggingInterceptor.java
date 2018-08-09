package com.batch.zSpringBootBatch.ex;



import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

public class LoggingInterceptor implements ClientHttpRequestInterceptor{

	  final static Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

		
	   @Override
	   public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		    
	        logger.info("Request: URI={}, getAccept={}, getContentType={}", httpRequest.getURI(), httpRequest.getHeaders().getAccept(), httpRequest.getHeaders().getContentType());
	        ClientHttpResponse response = new BufferingClientHttpResponseWrapper(clientHttpRequestExecution.execute(httpRequest, bytes));
	        if (!response.getStatusCode().is2xxSuccessful()) {
	        	 logger.info("RuntimeException={}","Api failed. Response: Status=" + response.getStatusCode() +", Body=" + StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
	        	 throw new RuntimeException("Api failed. Response: Status=" + response.getStatusCode());
	        }
	        logger.info("Response: getStatusCode={}",response.getStatusCode());
	        return response;
	    }
	   
	   private static class BufferingClientHttpResponseWrapper implements ClientHttpResponse {
		    private final ClientHttpResponse response;
		    private byte[] body;

		    public BufferingClientHttpResponseWrapper(ClientHttpResponse response) {
		      this.response = response;
		    }

		    public HttpStatus getStatusCode() throws IOException {
		      return this.response.getStatusCode();
		    }

		    public int getRawStatusCode() throws IOException {
		      return this.response.getRawStatusCode();
		    }

		    public String getStatusText() throws IOException {
		      return this.response.getStatusText();
		    }

		    public HttpHeaders getHeaders() {
		      return this.response.getHeaders();
		    }

		    public InputStream getBody() throws IOException {
		      if (this.body == null) {
		        this.body = StreamUtils.copyToByteArray(this.response.getBody());
		      }
		      return new ByteArrayInputStream(this.body);
		    }

		    public void close() {
		      this.response.close();
		    }
		  }
}
