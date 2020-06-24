/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.FeedbackMapper;
import com.igloo.smarthome.model.Feedback;

/**
 * 
 * @author shiwei
 * @date 2018年8月25日
 */
@Service
public class FeedbackService {
	
	@Autowired
	FeedbackMapper feedbackMapper;
	
	public void addFeedback(Feedback feedback) {
		this.feedbackMapper.insertSelective(feedback);
	}
	
	public void deleteFeedback(String id) {
		this.feedbackMapper.deleteByPrimaryKey(id);
	}
	
	public List<Feedback> getFeedback(String userId) {
		return this.feedbackMapper.getFeedback(userId);
	}
}
