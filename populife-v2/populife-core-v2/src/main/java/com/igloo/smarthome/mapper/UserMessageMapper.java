/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.igloo.smarthome.model.UserMessage;

import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author shiwei
 * @date 2018年8月26日
 */
public interface UserMessageMapper extends Mapper<UserMessage> {
	
	List<UserMessage> getUserMessage(@Param("userId") String userId, @Param("start") Integer start, @Param("limit") Integer limit);

	void updateInitLockUserMessage(@Param("userId") String userId, @Param("content") String content, @Param("lockId") Integer lockId);
}
