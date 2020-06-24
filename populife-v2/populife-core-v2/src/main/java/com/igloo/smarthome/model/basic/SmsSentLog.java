/*
 * Copyright (c) 2017, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model.basic;

/**
 * 
 * @author shiwe
 * @date 2019年3月19日
 */
public class SmsSentLog extends AbstractSentLog {
	
	String phoneNumber, phoneNumberPrefix;
	
	/**
	 * @return the phoneNumber
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * @param phoneNumber the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @return the phoneNumberPrefix
	 */
	public String getPhoneNumberPrefix() {
		return phoneNumberPrefix;
	}

	/**
	 * @param phoneNumberPrefix the phoneNumberPrefix to set
	 */
	public void setPhoneNumberPrefix(String phoneNumberPrefix) {
		this.phoneNumberPrefix = phoneNumberPrefix;
	}

}
