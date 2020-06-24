/*
 * Copyright (c) 2017, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model.basic;

import java.util.Date;

import javax.persistence.Id;

/**
 * 
 * @author Ares we
 * @date 2020年5月22日
 */
public abstract class AbstractSentLog {
	
	@Id
	String id;
	
	String varValues;
	
	Date createDate;
	
	Boolean successed;

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
	 * @return the varValues
	 */
	public String getVarValues() {
		return varValues;
	}

	/**
	 * @param varValues the varValues to set
	 */
	public void setVarValues(String varValues) {
		this.varValues = varValues;
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
	 * @return the successed
	 */
	public Boolean getSuccessed() {
		return successed;
	}

	/**
	 * @param successed the successed to set
	 */
	public void setSuccessed(Boolean successed) {
		this.successed = successed;
	}

}
