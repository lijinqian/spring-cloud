/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.ext.PushService;
import com.igloo.smarthome.ext.ScienerService;
import com.igloo.smarthome.mapper.KeyMapper;
import com.igloo.smarthome.mapper.LockMapper;
import com.igloo.smarthome.model.Key;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.User;

import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.ext.SystemException;
import tcsyn.basic.util.HttpClientUtil;
import tcsyn.basic.util.JsonUtil;

/**
 * 
 * @author shiwei
 * @date 2018年9月30日
 */
@Service
public class GatewayService {
	
	@Autowired
	LockMapper lockMapper;
	
	@Autowired
	ScienerService scienerService;
	
	@Value("${sciener.appid}")
	String appid;
	
	@Autowired
	KeyMapper keyMapper;
	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	PushService pushService;
	
	@Autowired
	UserService userService;
	
	@Transactional
	public void unfreeze(String userId, Integer lockId) {
		Lock lock = new Lock();
		lock.setLockId(lockId);
		lock.setStatus(Constants.LockStatus.NORMAL);
		this.lockMapper.updateByPrimaryKeySelective(lock);
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("lockId", lockId.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/unfreeze", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errorcode = (Integer) resultMap.get("errcode");
			if (errorcode != 0) {
				throw new IllegalStateException(result);
			}
			Key key = new Key();
			key.setLockId(lockId);
			key.setKeyStatus(Constants.KeyStatus.NORMAL);
			List<User> userList = this.userService.getByKey(key);
			for (User user : userList) {
				this.pushService.send(user, AbstractController.getText("The key has been thawed"), 10);
			}
		} catch (Exception e) {
			throw new SystemException("网关解冻锁失败", e);
		}
	}
	
	@Transactional
	public void freeze(String userId, Integer lockId) {
		Lock lock = new Lock();
		lock.setLockId(lockId);
		lock.setStatus(Constants.LockStatus.FREEZE);
		this.lockMapper.updateByPrimaryKeySelective(lock);
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("lockId", lockId.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/freeze", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errorcode = (Integer) resultMap.get("errcode");
			if (errorcode != 0) {
				throw new IllegalStateException(result);
			}
			Key key = new Key();
			key.setLockId(lockId);
			key.setKeyStatus(Constants.KeyStatus.NORMAL);
			List<User> userList = this.userService.getByKey(key);
			for (User user : userList) {
				this.pushService.send(user, AbstractController.getText("key.freeze.title"), 9);
			}
		} catch (Exception e) {
			throw new SystemException("网关冻结锁失败", e);
		}
	}
}
