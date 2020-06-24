/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.igloo.smarthome.model.Key;
import com.igloo.smarthome.model.User;

import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author shiwei
 * @date 2018年8月19日
 */
public interface UserMapper extends Mapper<User> {
	
	void updateUserDevice(@Param("userId") String userId, @Param("deviceId") String deviceId);
	
	List<User> getByKey(Key key);
	
	@Select("select u.* from user u where u.phone is null and u.email is null")
	List<User> getLostUser();
}
