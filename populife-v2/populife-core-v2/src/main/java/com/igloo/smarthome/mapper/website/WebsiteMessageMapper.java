/*
 * Copyright (c) 2018-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper.website;

import java.util.List;

import com.igloo.smarthome.model.website.WebsiteMessage;

import tcsyn.basic.model.BaseQuery;
import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author Ares
 * @date 2019年8月9日
 */
public interface WebsiteMessageMapper extends Mapper<WebsiteMessage> {
	
	List<WebsiteMessage> getMessage(BaseQuery baseQuery);
}
