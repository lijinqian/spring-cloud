/*
 * Copyright (c) 2017, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model.basic;

/**
 * 
 * @author Ares we
 * @date 2020年5月22日
 */
public class EmailSentLog extends AbstractSentLog {
	
	String toEmailAddress;

	/**
	 * @return the toEmailAddress
	 */
	public String getToEmailAddress() {
		return toEmailAddress;
	}

	/**
	 * @param toEmailAddress the toEmailAddress to set
	 */
	public void setToEmailAddress(String toEmailAddress) {
		this.toEmailAddress = toEmailAddress;
	}
}
