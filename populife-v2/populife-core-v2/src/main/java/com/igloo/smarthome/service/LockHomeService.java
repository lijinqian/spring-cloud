/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.LockHomeMapper;
import com.igloo.smarthome.model.LockHome;

/**
 * 
 * @author Ares S
 * @date 2020-6-23
 */
@Service
public class LockHomeService {
	
	@Autowired
	LockHomeMapper lockHomeMapper;
	
	public void addLockHome(Integer lockId, String homeId) {
		this.lockHomeMapper.insertSelective(new LockHome(lockId, homeId));
	}
}
