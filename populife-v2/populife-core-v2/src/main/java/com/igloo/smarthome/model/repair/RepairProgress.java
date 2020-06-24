/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model.repair;

import java.util.Date;

import javax.persistence.Id;

/**
 * 
 * @author Ares S
 * @date 2020年6月8日
 */
public class RepairProgress {
	
	/**
	 * 
	 */
	public RepairProgress() {
		super();
	}

	/**
	 * @param applyNo
	 * @param progress
	 * @param remark
	 * @param createDate
	 */
	public RepairProgress(String applyNo, Integer status, String remark, Date createDate) {
		super();
		this.applyNo = applyNo;
		this.status = status;
		this.remark = remark;
		this.createDate = createDate;
	}

	@Id
	String applyNo;
	
	@Id
	Integer status;
	
	String remark;
	
	Date createDate;

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
	 * @return the remark
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 * @param remark the remark to set
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 * @return the createDate
	 */
	public Date getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate the createDate to set
	 */
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
}
