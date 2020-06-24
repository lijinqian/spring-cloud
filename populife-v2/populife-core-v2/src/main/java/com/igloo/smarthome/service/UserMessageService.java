/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.ext.PushService;
import com.igloo.smarthome.mapper.UserMessageMapper;
import com.igloo.smarthome.model.User;
import com.igloo.smarthome.model.UserMessage;

import tcsyn.basic.util.TextUtil;

/**
 * 
 * @author shiwei
 * @date 2018年8月26日
 */
@Service
public class UserMessageService {
	
	@Autowired
	UserMessageMapper userMessageMapper;
	
	@Autowired
	PushService pushService;
	
	@Autowired
	UserService userSerivce;
	
	@Async
	public void batchAddUserMessage(List<String> userIds, String title, String content, Integer event) {
		for(String userId : userIds){
			this.addUserMessage(userId, title, content, event);
		}
	}
	
	public void addInitLockUserMessage(String userId, String title, String content, Integer  lockId) {
		UserMessage userMessage = new UserMessage();
		userMessage.setId(TextUtil.generateId());
		userMessage.setContent(content);
		userMessage.setTitle(title);
		userMessage.setUserId(userId);
		userMessage.setHasRead(Constants.NO);
		userMessage.setCreateDate(new Date());
		userMessage.setLockId(lockId);
		this.userMessageMapper.insertSelective(userMessage);
		
	}
	 
	public void addUserMessage(String userId, String title, String content, Integer event) {
		UserMessage userMessage = new UserMessage();
		userMessage.setId(TextUtil.generateId());
		userMessage.setContent(content);
		userMessage.setTitle(title);
		userMessage.setUserId(userId);
		userMessage.setHasRead(Constants.NO);
		userMessage.setCreateDate(new Date());
		this.userMessageMapper.insertSelective(userMessage);
		
		if (event != null) {
			User user = this.userSerivce.getById(userId);
			this.pushService.send(user, content, event);
		}
	}
	
	public List<UserMessage> getUserMessage(String userId, Integer start, Integer limit) {
		return this.userMessageMapper.getUserMessage(userId, start, limit);
	}
	
	public UserMessage getById(String id) {
		return this.userMessageMapper.selectByPrimaryKey(id);
	}
	
	public void updateUserMessage(UserMessage userMessage) {
		this.userMessageMapper.updateByPrimaryKeySelective(userMessage);
	}
	
	public void deleteByUserId(String userId) {
		UserMessage userMessage = new UserMessage();
		userMessage.setUserId(userId);
		this.userMessageMapper.delete(userMessage);
	}
	
	public void deleteById(String id) {
		this.userMessageMapper.deleteByPrimaryKey(id);
	}

	public void updateInitLockUserMessage(String userId, String content, Integer lockId) {
		this.userMessageMapper.updateInitLockUserMessage(userId,  content,  lockId);
		
	}
}
