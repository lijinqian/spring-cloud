/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * 家庭（锁分组）
 * @author shiwei
 * @date 2018年8月25日
 */
public class Home {
	
	/**
	 * 
	 */
	public Home() {
		super();
	}

	/**
	 * @param id
	 * @param name
	 * @param country
	 * @param timeZone
	 * @param userId
	 * @param createDate
	 * @param lockCount
	 */
	public Home(String id, String name, String country, String timeZone, String userId, Date createDate,
			Integer lockCount) {
		super();
		this.id = id;
		this.name = name;
		this.country = country;
		this.timeZone = timeZone;
		this.userId = userId;
		this.createDate = createDate;
		this.lockCount = lockCount;
	}

	@Id
	String id;
	
	String name, country, timeZone, userId;
	
	Date createDate;
	
	@Transient
	Integer lockCount;
	
	

	/**
	 * @return the lockCount
	 */
	public Integer getLockCount() {
		return lockCount;
	}

	/**
	 * @param lockCount the lockCount to set
	 */
	public void setLockCount(Integer lockCount) {
		this.lockCount = lockCount;
	}

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
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
}
