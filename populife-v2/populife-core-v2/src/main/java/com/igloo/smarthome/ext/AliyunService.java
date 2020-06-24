/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.ext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.igloo.smarthome.model.basic.SentExceptionLog;
import com.igloo.smarthome.model.basic.SmsSentLog;
import com.igloo.smarthome.service.basic.SmsSentLogService;
import com.twilio.sdk.Twilio;
import com.twilio.sdk.resource.api.v2010.account.Message;
import com.twilio.sdk.type.PhoneNumber;

import tcsyn.basic.util.JsonUtil;
import tcsyn.basic.util.TextUtil;

/**
 * 
 * @author shiwei
 * @date 2018年9月25日
 */
@Component
public class AliyunService {
	
	@Value("${aliyun.accessKeyId}")
	String accessKeyId;
	
	@Value("${aliyun.accessKeySecret}")
	String accessKeySecret;
	
	@Value("${aliyun.sms.sign}")
	String sign;
	
	@Value("${aliyun.sms.templatecode}")
	String templateCode;
	
	@Value("${aliyun.sms.i18nTemplateCode}")
	String i18nTemplateCode;
	
	@Value("${twilio.sid}")
	String twilioSid;
	
	@Value("${twilio.token}")
	String twilioToken;
	
	@Value("${twilio.sender}")
	String twilioSender;
	
	@Value("${twilio.text}")
	String twilioText;
	
	@Autowired
	SmsSentLogService smsSentLogService;
	
	IAcsClient acsClient;
	
	Logger logger = Logger.getLogger(this.getClass());

	@PostConstruct
	public void init() {
		// 设置超时时间-可自行调整
		System.setProperty("sun.net.client.defaultConnectTimeout", "7000");
		System.setProperty("sun.net.client.defaultReadTimeout", "7000");
		// 初始化ascClient需要的几个参数
		final String product = "Dysmsapi";// 短信API产品名称（短信产品名固定，无需修改）
		final String domain = "dysmsapi.aliyuncs.com";// 短信API产品域名（接口地址固定，无需修改）
		// 初始化ascClient,暂时不支持多region（请勿修改）
		IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
		try {
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
		} catch (ClientException e) {
			logger.error(e.getMessage(), e);
		}
		this.acsClient = new DefaultAcsClient(profile);
		
		Twilio.init(this.twilioSid, this.twilioToken);
	}
	
	public SendSmsResponse sendSms(String phone, String code, String countryCode) {
		String sentLogId = TextUtil.generateId();
		SmsSentLog smsSentLog = new SmsSentLog();
		smsSentLog.setId(sentLogId);
		smsSentLog.setCreateDate(new Date());
		smsSentLog.setPhoneNumber(phone);
		smsSentLog.setPhoneNumberPrefix(countryCode);
		smsSentLog.setSuccessed(false);
		if (phone.startsWith("+86")) {
			// 国内手机号
			phone = phone.substring(3);
			SendSmsResponse sendSmsResponse = null;
			SendSmsRequest request = new SendSmsRequest();
			request.setMethod(MethodType.POST);
			request.setPhoneNumbers(phone);
			request.setSignName(this.sign);
			request.setTemplateCode(this.templateCode);
			Map<String, String> map = new HashMap<>();
			map.put("code", code);
			request.setTemplateParam(JsonUtil.toJson(map));
			smsSentLog.setVarValues(request.getTemplateParam());
			// 请求失败这里会抛ClientException异常
			try {
				sendSmsResponse = this.acsClient.getAcsResponse(request);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				this.smsSentLogService.addSmsSentLog(smsSentLog, new SentExceptionLog(sentLogId, ExceptionUtils.getStackTrace(e)));
			}
			if (sendSmsResponse != null) {
				if (!sendSmsResponse.getCode().equals("OK")) {
					this.smsSentLogService.addSmsSentLog(smsSentLog, new SentExceptionLog(sentLogId, "code: " + sendSmsResponse.getCode() + ", message: " + sendSmsResponse.getMessage()));
				}
			}
			smsSentLog.setSuccessed(true);
			this.smsSentLogService.addSmsSentLog(smsSentLog);
			return sendSmsResponse;
		} else {
			// 国际短信
			Message message = null;
			try {
				message = Message.create(this.twilioSid, new PhoneNumber(phone), new PhoneNumber(this.twilioSender), this.twilioText + code).execute();
			} catch (Exception e) {
				this.smsSentLogService.addSmsSentLog(smsSentLog, new SentExceptionLog(sentLogId, ExceptionUtils.getStackTrace(e)));
			}
			if (StringUtils.equals(message.getStatus().toString(), "failed")) {
				this.smsSentLogService.addSmsSentLog(smsSentLog, new SentExceptionLog(sentLogId, message.getErrorMessage()));
				throw new IllegalStateException("Sent twilio sms failed, phone number is " + phone);
			}
			smsSentLog.setSuccessed(true);
			this.smsSentLogService.addSmsSentLog(smsSentLog);
			return null;
		}
		
	}
}
