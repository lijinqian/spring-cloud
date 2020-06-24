/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model.repair;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 
 * @author Ares S
 * @date 2020年6月8日
 */
public class RepairApply {
	
	/**
	 * 
	 */
	public RepairApply() {
		super();
	}

	/**
	 * @param applyNo
	 * @param status
	 */
	public RepairApply(String applyNo, Integer status) {
		super();
		this.applyNo = applyNo;
		this.status = status;
	}

	@Id
	String applyNo;
	
	String userId, description, modelNum, purchasedTicket;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	Date purchasedDate;
	
	Date createDate;
	  
	Boolean cancelled;
	
	Integer status;
	
	@Transient
	String lockName;
	
	public boolean isEmpty() {
		return StringUtils.isAnyBlank(this.userId, this.description, this.modelNum);
	}

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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the modelNum
	 */
	public String getModelNum() {
		return modelNum;
	}

	/**
	 * @param modelNum the modelNum to set
	 */
	public void setModelNum(String modelNum) {
		this.modelNum = modelNum;
	}

	/**
	 * @return the purchasedTicket
	 */
	public String getPurchasedTicket() {
		return purchasedTicket;
	}

	/**
	 * @param purchasedTicket the purchasedTicket to set
	 */
	public void setPurchasedTicket(String purchasedTicket) {
		this.purchasedTicket = purchasedTicket;
	}

	/**
	 * @return the purchasedDate
	 */
	public Date getPurchasedDate() {
		return purchasedDate;
	}

	/**
	 * @param purchasedDate the purchasedDate to set
	 */
	public void setPurchasedDate(Date purchasedDate) {
		this.purchasedDate = purchasedDate;
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
	 * @return the cancelled
	 */
	public Boolean getCancelled() {
		return cancelled;
	}

	/**
	 * @param cancelled the cancelled to set
	 */
	public void setCancelled(Boolean cancelled) {
		this.cancelled = cancelled;
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

	/**
	 * @return the lockName
	 */
	public String getLockName() {
		return lockName;
	}

	/**
	 * @param lockName the lockName to set
	 */
	public void setLockName(String lockName) {
		this.lockName = lockName;
	}

}
