/*
 * Copyright (c) 2017, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.basic.SentExceptionLogMapper;
import com.igloo.smarthome.mapper.basic.SmsSentLogMapper;
import com.igloo.smarthome.model.basic.SentExceptionLog;
import com.igloo.smarthome.model.basic.SmsSentLog;

/**
 * 
 * @author Ares we
 * @date 2020年5月22日
 */
@Service
public class SmsSentLogService {

	@Autowired
	SmsSentLogMapper smsSentLogMapper;
	
	@Autowired
	SentExceptionLogMapper sentExceptionLogMapper;
	
	public void addSmsSentLog(SmsSentLog smsSentLog) {
		this.smsSentLogMapper.insertSelective(smsSentLog);
	}
	
	public void addSmsSentLog(SmsSentLog smsSentLog, SentExceptionLog sentExceptionLog) {
		this.smsSentLogMapper.insertSelective(smsSentLog);
		this.sentExceptionLogMapper.insertSelective(sentExceptionLog);
	}
}
