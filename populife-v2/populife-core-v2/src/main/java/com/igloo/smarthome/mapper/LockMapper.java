/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.vo.ExpireKeyVo;
import com.igloo.smarthome.model.vo.LockSetupVo;
import com.igloo.smarthome.model.vo.LockUserVo;
import com.igloo.smarthome.model.vo.LockVo;

import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author lijq
 * @Date 2018年8月21日
 */
public interface LockMapper extends Mapper<Lock> {
	
	void updateHomeId(@Param("oldHomeId") String oldHomeId, @Param("newHomeId") String newHomeId);

	List<LockUserVo> getUserList(@Param("lockId") Integer lockId, @Param("userId") String userId, 
			@Param("start") Integer start, @Param("pageSize") Integer pageSize);

	List<ExpireKeyVo> getExpireKeyList(@Param("lockId") Integer lockId, @Param("userId") String userId, 
			@Param("start") Integer start, @Param("pageSize") Integer pageSize, @Param("expireDays") Integer expireDays);

	LockSetupVo getManagerSetup(@Param("userId") String userId, @Param("lockId") Integer lockId);

	LockSetupVo getNormalSetup(@Param("userId") String userId, @Param("lockId") Integer lockId);

	List<LockVo> list(@Param("userId") String userId, @Param("start") Integer start, @Param("pageSize") Integer pageSize);
	
	void transfer(@Param("id") String id, @Param("lockIds") String[] lockIds);
	
	Lock getUndeletedByMac(@Param("mac") String mac);
}
