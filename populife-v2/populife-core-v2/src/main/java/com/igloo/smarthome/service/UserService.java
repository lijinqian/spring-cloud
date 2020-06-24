/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.mapper.HomeMapper;
import com.igloo.smarthome.mapper.UserMapper;
import com.igloo.smarthome.model.Home;
import com.igloo.smarthome.model.Key;
import com.igloo.smarthome.model.User;

import tcsyn.basic.ext.SystemException;
import tcsyn.basic.util.HttpClientUtil;
import tcsyn.basic.util.JsonUtil;
import tcsyn.basic.util.TextUtil;

/**
 * 
 * @author shiwei
 * @date 2018年8月19日
 */
@Service
public class UserService {
	
	@Autowired
	UserMapper userMapper;
	
	
	@Value("${sciener.appid}")
	String appid;
	
	@Value("${sciener.appsecret}")
	String appsecret;
	
	@Autowired
	HomeMapper homeMapper;
	
	public User getById(String userId) {
		return this.userMapper.selectByPrimaryKey(userId);
	}
	
	public void updateUser(User user) {
		this.userMapper.updateByPrimaryKeySelective(user);
	}
	
	public void updateUserAndDevice(User user) {
		this.updateUser(user);
		this.updateUserDevice(user.getId(), user.getDeviceId());
	}
	
	public void updateUserDevice(String userId, String deviceId) {
		this.userMapper.updateUserDevice(userId, deviceId);
	}
	
	public User getByPhone(String phone) {
		User user = new User();
		user.setPhone(phone);
		user.setIsDeleted(Constants.NO);
		return this.userMapper.selectOne(user);
	}
	
	public User getByEmail(String email) {
		User user = new User();
		user.setEmail(email);
		user.setIsDeleted(Constants.NO);
		return this.userMapper.selectOne(user);
	}
	
	@Transactional
	public void addUser(User user) {
		// 保障事务一致性，先执行本操作，再调用远程接口
		String password = user.getPassword();
		user.setPassword(TextUtil.md5(password, 3));
		this.userMapper.insertSelective(user);
		this.homeMapper.insertSelective(new Home(TextUtil.generateId(), "My Space", null, null, user.getId(), user.getRegisteredDate(), 0));
		this.updateUserDevice(user.getId(), user.getDeviceId());
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("clientSecret", this.appsecret);
		paramMap.put("username", user.getId());
		paramMap.put("password", TextUtil.md5(password, 4));
		String date = String.valueOf(user.getRegisteredDate().getTime());
		paramMap.put("date", date);
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "user/register", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			String username = (String) resultMap.get("username");
			if (StringUtils.isBlank(username)) {
				throw new IllegalStateException(result);
			}
			User modifyUser = new User();
			modifyUser.setId(user.getId());
			modifyUser.setUsername(username);
			this.userMapper.updateByPrimaryKeySelective(modifyUser);
		} catch (Exception e) {
			throw new SystemException("Registration failed, please try again later", e);
		}
	}

	@Transactional
	public void resetPassword(User user) {
		this.userMapper.updateByPrimaryKeySelective(user);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("clientSecret", this.appsecret);
		paramMap.put("username", user.getUsername());
		paramMap.put("password", TextUtil.md5(user.getPassword(), 1));
		paramMap.put("date", String.valueOf((new Date()).getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "user/resetPassword", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
		} catch (Exception e) {
			throw new SystemException("Password reset failed, please try again later", e);
		}
		
	}
	
	@Transactional
	public void deleteUser(User user) {
		user.setIsDeleted(Constants.YES);
		user.setDeviceId(null);
		user.setApnsToken(null);
		this.userMapper.updateByPrimaryKey(user);
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("clientSecret", this.appsecret);
		paramMap.put("username", user.getUsername());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "user/delete", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if (0 != MapUtils.getInteger(resultMap, "errcode")) {
				throw new IllegalStateException(result);
			}
		} catch (Exception e) {
			throw new SystemException("Deleting user failed, please try again later", e);
		}
	}
	
	public User getUserById(String userId) {
		return this.userMapper.selectByPrimaryKey(userId);
	}

	
	public List<User> getByKey(Key key) {
		return this.userMapper.getByKey(key);
	}
}
