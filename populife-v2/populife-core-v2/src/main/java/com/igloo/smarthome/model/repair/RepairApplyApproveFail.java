/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model.repair;

import javax.persistence.Id;

/**
 * 
 * @author Ares S
 * @date 2020年6月11日
 */
public class RepairApplyApproveFail {
	
	@Id
	String applyNo;
	
	String failedReason;

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
	 * @return the failedReason
	 */
	public String getFailedReason() {
		return failedReason;
	}

	/**
	 * @param failedReason the failedReason to set
	 */
	public void setFailedReason(String failedReason) {
		this.failedReason = failedReason;
	}
	
}
