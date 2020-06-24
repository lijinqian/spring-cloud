/*
 * Copyright (c) 2017, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model.basic;

import javax.persistence.Id;

/**
 * 
 * @author shiwe
 * @date 2019年3月19日
 */
public class SentExceptionLog {
	
	/**
	 * @param sentLogId
	 * @param exStackInfo
	 */
	public SentExceptionLog(String sentLogId, String exStackInfo) {
		super();
		this.sentLogId = sentLogId;
		this.exStackInfo = exStackInfo;
	}

	/**
	 * 
	 */
	public SentExceptionLog() {
		super();
	}

	@Id
	String sentLogId;
	
	String exStackInfo;

	/**
	 * @return the sentLogId
	 */
	public String getSentLogId() {
		return sentLogId;
	}

	/**
	 * @param sentLogId the sentLogId to set
	 */
	public void setSentLogId(String sentLogId) {
		this.sentLogId = sentLogId;
	}

	/**
	 * @return the exStackInfo
	 */
	public String getExStackInfo() {
		return exStackInfo;
	}

	/**
	 * @param exStackInfo the exStackInfo to set
	 */
	public void setExStackInfo(String exStackInfo) {
		this.exStackInfo = exStackInfo;
	}
}
