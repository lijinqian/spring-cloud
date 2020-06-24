/*
 * Copyright (c) 2017, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper.website;

import java.util.List;

import com.igloo.smarthome.model.website.SubscribeEmail;

import tcsyn.basic.model.BaseQuery;
import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author shiwe
 * @date 2019年1月9日
 */
public interface SubscribeEmailMapper extends Mapper<SubscribeEmail> {
	
	List<SubscribeEmail> getSubscribeEmail(BaseQuery baseQuery);
}
