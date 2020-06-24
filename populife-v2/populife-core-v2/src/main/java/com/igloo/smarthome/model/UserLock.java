/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model;

import javax.persistence.Id;

/**
 * 用户关联的锁
 * @author shiwei
 * @date 2018年8月27日
 */
public class UserLock {
	
	/**
	 * 
	 */
	public UserLock() {
		super();
	}

	/**
	 * @param userId
	 * @param lockId
	 */
	public UserLock(String userId, Integer lockId) {
		super();
		this.userId = userId;
		this.lockId = lockId;
	}

	@Id
	String userId;
	
	@Id
	Integer lockId;
	
	String homeId;

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the lockId
	 */
	public Integer getLockId() {
		return lockId;
	}

	/**
	 * @param lockId the lockId to set
	 */
	public void setLockId(Integer lockId) {
		this.lockId = lockId;
	}

	/**
	 * @return the homeId
	 */
	public String getHomeId() {
		return homeId;
	}

	/**
	 * @param homeId the homeId to set
	 */
	public void setHomeId(String homeId) {
		this.homeId = homeId;
	}
}
