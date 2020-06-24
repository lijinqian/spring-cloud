/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.basic;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.basic.UserLogMapper;
import com.igloo.smarthome.model.basic.UserLog;

import tcsyn.basic.util.TextUtil;

/**
 * 
 * @author Ares S
 * @date 2020年5月25日
 */
@Service
public class UserLogService {
	
	@Autowired
	UserLogMapper userLogMapper;
	
	Logger logger = Logger.getLogger(this.getClass());
	
	public void addUserLog(Date createDate, String paramValues, String userId, Boolean processSuccessed, String title, String responseResult) {
		try {
			UserLog userLog = new UserLog();
			userLog.setId(TextUtil.generateId());
			userLog.setCreateDate(createDate);
			userLog.setParamValues(paramValues);
			userLog.setUserId(userId);
			userLog.setProcessSuccessed(processSuccessed);
			userLog.setTitle(title);
			userLog.setResponseResult(responseResult);
			this.userLogMapper.insertSelective(userLog);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
