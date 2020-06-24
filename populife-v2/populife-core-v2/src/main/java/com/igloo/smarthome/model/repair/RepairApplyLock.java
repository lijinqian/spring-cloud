/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model.repair;

import javax.persistence.Id;

/**
 * 
 * @author Ares S
 * @date 2020年6月8日
 */
public class RepairApplyLock {
	
	/**
	 * 
	 */
	public RepairApplyLock() {
		super();
	}

	/**
	 * @param applyNo
	 * @param lockId
	 */
	public RepairApplyLock(String applyNo, Integer lockId) {
		super();
		this.applyNo = applyNo;
		this.lockId = lockId;
	}

	@Id
	String applyNo;
	
	Integer lockId;

	/**
	 * @return the applyNo
	 */
	public String getApplyNo() {
		return applyNo;
	}

	/**
	 * @param applyNo the applyNo to set
	 */
	public void setApplyNo(String applyNo) {
		this.applyNo = applyNo;
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
}
