/*
 * Copyright (c) 2017, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.basic.EmailSentLogMapper;
import com.igloo.smarthome.mapper.basic.SentExceptionLogMapper;
import com.igloo.smarthome.model.basic.EmailSentLog;
import com.igloo.smarthome.model.basic.SentExceptionLog;

/**
 * 
 * @author Ares we
 * @date 2020年5月22日
 */
@Service
public class EmailSentLogService {
	
	@Autowired
	EmailSentLogMapper emailSentLogMapper;
	
	@Autowired
	SentExceptionLogMapper sentExceptionLogMapper;
	
	public void addEmailSentLog(EmailSentLog emailSentLog) {
		this.emailSentLogMapper.insertSelective(emailSentLog);
	}
	
	public void addEmailSentLog(EmailSentLog emailSentLog, SentExceptionLog sentExceptionLog) {
		this.emailSentLogMapper.insertSelective(emailSentLog);
		this.sentExceptionLogMapper.insertSelective(sentExceptionLog);
	}
}
