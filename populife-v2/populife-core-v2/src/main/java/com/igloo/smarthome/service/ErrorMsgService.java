/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.ErrorMsgMapper;
import com.igloo.smarthome.model.vo.ErrorMsgVo;

/**
 * 
 * @author lijq
 * @Date 2018年8月25日
 */
@Service
public class ErrorMsgService {
	
	final String dateFormat = "yyyy-MM-dd HH:mm";
	
	@Autowired
	ErrorMsgMapper errorMsgMapper;
	
	public List<ErrorMsgVo> list(String userid){
		return errorMsgMapper.list(userid);
	}
	
	public void insert(String userid,String msg){
		errorMsgMapper.insertMsg(userid, msg);
	}
	
}
