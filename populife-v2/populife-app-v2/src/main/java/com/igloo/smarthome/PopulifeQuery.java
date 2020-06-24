/*
 * Copyright (c) 2017, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome;

import tcsyn.basic.model.BaseQuery;

/**
 * 
 * @author shiwe
 * @date 2019年1月9日
 */
public class PopulifeQuery extends BaseQuery {
	
	String keyword;

	/**
	 * @return the keyword
	 */
	public String getKeyword() {
		return keyword;
	}

	/**
	 * @param keyword the keyword to set
	 */
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
