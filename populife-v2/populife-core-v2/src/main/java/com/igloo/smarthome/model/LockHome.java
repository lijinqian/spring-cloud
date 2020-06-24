/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model;

import javax.persistence.Id;

/**
 * 
 * @author Ares S
 * @date 2020-6-23
 */
public class LockHome {
	
	/**
	 * 
	 */
	public LockHome() {
		super();
	}

	/**
	 * @param lockId
	 * @param homeId
	 */
	public LockHome(Integer lockId, String homeId) {
		super();
		this.lockId = lockId;
		this.homeId = homeId;
	}

	@Id
	Integer lockId;
	
	String homeId;

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
