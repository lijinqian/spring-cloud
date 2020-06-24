/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.HomeMapper;
import com.igloo.smarthome.mapper.LockMapper;
import com.igloo.smarthome.mapper.UserLockMapper;
import com.igloo.smarthome.model.Home;
import com.igloo.smarthome.model.UserLock;

/**
 * 
 * @author shiwei
 * @date 2018年8月25日
 */
@Service
public class HomeService {
	
	@Autowired
	HomeMapper homeMapper;
	
	@Autowired
	LockMapper lockMapper;
	
	@Autowired
	UserLockMapper userLockMapper;
	
	public Home getByName(Home home) {
		return this.homeMapper.getByName(home);
	}
	
	public void addHome(Home home) {
		this.homeMapper.insertSelective(home);
	}
	
	public void updateHome(Home home) {
		this.homeMapper.updateByPrimaryKeySelective(home);
	}
	
	public void deleteHome(String id) {
		// 清空锁分组id
		UserLock userLock = new UserLock();
		userLock.setHomeId(id);
		List<UserLock> userLockList = this.userLockMapper.select(userLock);
		for (UserLock ul : userLockList) {
			ul.setHomeId(null);
			this.userLockMapper.updateByPrimaryKey(ul);
		}
		this.homeMapper.deleteByPrimaryKey(id);
	}
	
	public List<Home> getHome(String userId) {
		return this.homeMapper.getHome(userId);
	}
}
