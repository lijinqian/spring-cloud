/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model.basic;

import java.util.Date;

import javax.persistence.Id;

/**
 * 
 * @author Ares S
 * @date 2020年5月25日
 */
public class UserLog {
	
	@Id
	String id;
	
	String title, userId, paramValues, responseResult;
	
	Date createDate;
	
	Boolean processSuccessed;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
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
	 * @return the paramValues
	 */
	public String getParamValues() {
		return paramValues;
	}

	/**
	 * @param paramValues the paramValues to set
	 */
	public void setParamValues(String paramValues) {
		this.paramValues = paramValues;
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
	 * @return the processSuccessed
	 */
	public Boolean getProcessSuccessed() {
		return processSuccessed;
	}

	/**
	 * @param processSuccessed the processSuccessed to set
	 */
	public void setProcessSuccessed(Boolean processSuccessed) {
		this.processSuccessed = processSuccessed;
	}

	/**
	 * @return the responseResult
	 */
	public String getResponseResult() {
		return responseResult;
	}

	/**
	 * @param responseResult the responseResult to set
	 */
	public void setResponseResult(String responseResult) {
		this.responseResult = responseResult;
	}
}
