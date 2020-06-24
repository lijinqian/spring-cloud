/*
 * Copyright (c) 2017, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.website;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.website.SubscribeEmailMapper;
import com.igloo.smarthome.model.website.SubscribeEmail;

import tcsyn.basic.model.BaseQuery;
import tcsyn.basic.model.PagerModel;

/**
 * 
 * @author shiwe
 * @date 2019年1月9日
 */
@Service
public class SubscribeEmailService {
	
	@Autowired
	SubscribeEmailMapper subscribeEmailMapper;
	
	public PagerModel<SubscribeEmail> getSubscribeEmail(BaseQuery baseQuery) {
		List<SubscribeEmail> dataList = this.subscribeEmailMapper.getSubscribeEmail(baseQuery);
		return PagerModel.build(baseQuery, dataList);
	}
	
	public SubscribeEmail getById(String email) {
		return this.subscribeEmailMapper.selectByPrimaryKey(email);
	}
	
	public void add(String email) {
		SubscribeEmail subscribeEmail = new SubscribeEmail();
		subscribeEmail.setEmail(email);
		subscribeEmail.setCreateDate(new Date());
		this.subscribeEmailMapper.insertSelective(subscribeEmail);
	}
}
