package com.batch.zSpringBootBatch.ex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


/**
 * tasklet을 이용 통째로 구현한다.
 * http://becko.tistory.com/category/Spring-Batch
 */
public class FtpTasklet implements Tasklet {

	@Value("${batch.ftp.server}")
	private String FTP_SERVER;
	@Value("${batch.ftp.user}")
	private String FTP_USER;
	@Value("${batch.ftp.password}")
	private String FTP_PASSWORD;
	@Value("${batch.ftp.path}")
	private String FTP_PATH;

	@Value("${batch.file.path}")
	private String FILE_PATH;
	@Value("${batch.file.prefix}")
	private String FILE_PREFIX;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		System.out.println("FtpTasklet contribution: {}"+contribution);
// https://blog.pavelsklenar.com/spring-integration-sftp-upload-example/
		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;

		JSch jsch = new JSch();
		try {
			// 세션객체 생성 ( user , host, port )
			session = jsch.getSession(FTP_USER, FTP_SERVER);

			// password 설정
			session.setPassword(FTP_PASSWORD);
			// 세션관련 설정정보 설정
			// 호스트 정보 검사하지 않는다.
			session.setConfig("StrictHostKeyChecking", "no");
			// 접속
			session.connect();
			// sftp 채널 접속
			channel = session.openChannel("sftp");
			channel.connect();
			System.out.println("FTP : Channel(id:{}) connected."+ channel.getId());
		} catch (JSchException e) {
			e.printStackTrace();
		}
		channelSftp = (ChannelSftp) channel;

		String dateFormat = DateFormatUtils.format(new Date(), "yyyyMMdd");
		File file = new File(FILE_PATH + FILE_PREFIX + dateFormat + ".txt");
		FileInputStream in = null;
		try { // 파일을 가져와서 inputStream에 넣고 저장경로를 찾아 put
			in = new FileInputStream(file);
			channelSftp.cd(FTP_PATH);
			channelSftp.put(in, file.getName());
			System.out.println("FTP : file ({}) sent."+ file.getName());
		} catch (SftpException se) {
			se.printStackTrace();
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		channelSftp.quit();
		session.disconnect();

		return RepeatStatus.FINISHED;
	}

}
