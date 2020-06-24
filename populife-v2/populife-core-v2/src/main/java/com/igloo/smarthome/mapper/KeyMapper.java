/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.igloo.smarthome.model.Key;
import com.igloo.smarthome.model.vo.KeyDetailVo;

import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author lijq
 * @Date 2018年8月21日
 */
public interface KeyMapper extends Mapper<Key> {

	KeyDetailVo getKeyDetail(@Param("keyId") Integer keyId);

	List<KeyDetailVo> list(@Param("userId") String userId, @Param("lockId") Integer lockId, @Param("start") Integer start, @Param("pageSize") Integer pageSize, @Param("isAdministrator") boolean isAdministrator);

	void updateKeyStatusByLockId(@Param("keyStatus") String keyStatus,  @Param("lockId") Integer lockId, @Param("isClear") String isClear);

	Key findNormalOne(@Param("userId") String userId, @Param("lockId") Integer lockId);
	
	Key findOne(@Param("userId") String userId, @Param("lockId") Integer lockId, @Param("keyId") Integer keyId);

	void updateKeyStatusByLockIdAndSender(@Param("keyStatus") String keyStatus, @Param("lockId") Integer lockId, @Param("userId")String userId);

	List<String> getAllUserIdsByLockId(@Param("lockId") Integer lockId, @Param("userId")String userId, @Param("isAdmin") Boolean isAdmin, @Param("isIncludeMe") Boolean isIncludeMe);

	Integer getNormalKeyCount(@Param("userId") String userId);

}
