/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.ext;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dm.model.v20151123.SingleSendMailRequest;
import com.aliyuncs.dm.model.v20151123.SingleSendMailResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.igloo.smarthome.model.basic.EmailSentLog;
import com.igloo.smarthome.model.basic.SentExceptionLog;
import com.igloo.smarthome.service.basic.EmailSentLogService;

import tcsyn.basic.util.TextUtil;

/**
 * 邮件服务
 * @author shiwei
 * @date 2018年8月19日
 */
@Component
public class EmailService {
	
	@Value("${email.sender}")
	String sender;
	
	@Value("${email.password}")
	String password;
	
	@Value("${email.host}")
	String host;
	
	@Value("${email.port}")
	Integer port;
	
	JavaMailSenderImpl javaMailSender = null;
	
	IAcsClient client = null;
	
	@Value("${aliyun.accessKey}")
	String accessKey;
	
	@Value("${aliyun.accessKeySecret2}")
	String accessKeySecret;
	
	@Value("${aliyun.email.accountName}")
	String accountName;
	
	@Value("${aliyun.email.tagName}")
	String tagName;
	
	@Autowired
	EmailSentLogService emailSentLogService;
	
	@PostConstruct
	public void init() {
//		javaMailSender = new JavaMailSenderImpl();
//        javaMailSender.setHost(this.host);
//        javaMailSender.setPort(this.port);
//        javaMailSender.setUsername(this.sender);
//        javaMailSender.setPassword(this.password);
//        Properties properties = new Properties();
//        properties.setProperty("mail.host", this.host);
//        properties.setProperty("mail.transport.protocol", "smtp");
//        properties.setProperty("mail.smtp.auth", "true");
//        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
//        properties.setProperty("mail.smtp.port", this.port.toString());
//        properties.setProperty("mail.smtp.socketFactory.port", this.port.toString());
//        javaMailSender.setJavaMailProperties(properties);
        
     // 如果是除杭州region外的其它region（如新加坡、澳洲Region），需要将下面的"cn-hangzhou"替换为"ap-southeast-1"、或"ap-southeast-2"。
 		IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey, accessKeySecret);
 		// 如果是除杭州region外的其它region（如新加坡region）， 需要做如下处理
 		// try {
 		// DefaultProfile.addEndpoint("dm.ap-southeast-1.aliyuncs.com",
 		// "ap-southeast-1", "Dm", "dm.ap-southeast-1.aliyuncs.com");
 		// } catch (ClientException e) {
 		// e.printStackTrace();
 		// }
 		client = new DefaultAcsClient(profile);
	}
	
	/**
	 * 发送邮件
	 * @param to
	 * @param subject
	 * @param text
	 */
	public void sendEmail(String to, String subject, String text, String code) {
		SingleSendMailRequest request = new SingleSendMailRequest();
		request.setConnectTimeout(10000);
		request.setReadTimeout(10000);
		// request.setVersion("2017-06-22");//
		// 如果是除杭州region外的其它region（如新加坡region）,必须指定为2017-06-22
		request.setAccountName(accountName);
		request.setFromAlias("Populife");
		request.setAddressType(1);
		request.setTagName(tagName);
		request.setReplyToAddress(true);
		request.setToAddress(to);
		// 可以给多个收件人发送邮件，收件人之间用逗号分开，批量发信建议使用BatchSendMailRequest方式
		// request.setToAddress("邮箱1,邮箱2");
		request.setSubject(subject);
		request.setHtmlBody(text);
		String sentLogId = TextUtil.generateId();
		EmailSentLog emailSentLog = new EmailSentLog();
		emailSentLog.setCreateDate(new Date());
		emailSentLog.setId(sentLogId);
		emailSentLog.setToEmailAddress(to);
		emailSentLog.setSuccessed(false);
		emailSentLog.setVarValues(code);
		try {
			SingleSendMailResponse response = client.getAcsResponse(request);
			emailSentLog.setSuccessed(true);
			this.emailSentLogService.addEmailSentLog(emailSentLog);
		} catch (Exception e) {
			e.printStackTrace();
			this.emailSentLogService.addEmailSentLog(emailSentLog, new SentExceptionLog(sentLogId, ExceptionUtils.getStackTrace(e)));
		}
	}
}
