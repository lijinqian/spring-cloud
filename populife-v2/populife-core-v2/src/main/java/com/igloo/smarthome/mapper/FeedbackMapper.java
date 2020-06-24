/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.igloo.smarthome.model.Feedback;

import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author shiwei
 * @date 2018年8月25日
 */
public interface FeedbackMapper extends Mapper<Feedback> {
	
	List<Feedback> getFeedback(@Param("userId") String userId);
}
