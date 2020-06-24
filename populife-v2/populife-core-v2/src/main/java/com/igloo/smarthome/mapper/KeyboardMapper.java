/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.igloo.smarthome.model.Keyboard;
import com.igloo.smarthome.model.vo.KeyboardPwdVo;

import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author lijq
 * @Date 2018年8月25日
 */
public interface KeyboardMapper extends Mapper<Keyboard> {

	List<KeyboardPwdVo> list(@Param("userId") String userId, @Param("isAdmin") Boolean isAdmin, 
			@Param("lockId") Integer lockId, @Param("start") Integer start, @Param("pageSize") Integer pageSize);

	void delKeyboardByLockId(@Param("delType") Integer delType, @Param("lockId") Integer lockId);

	void delKeyboardByLockIdAndSender(@Param("delType") Integer delType, @Param("lockId") Integer lockId, @Param("lockId") String userId);

	void updateNormalPwd2Deleted(@Param("lockId") Integer lockId, @Param("date") Date date);

	void deleteUsedKeyboard(@Param("lockId") Integer lockId, @Param("deleteType") Integer deleteType);

	Integer getUsedKeyboards(@Param("lockId") Integer lockId, @Param("password") String password);
	
	Integer checkUsedKeyboards(@Param("lockId") Integer lockId, @Param("password") String password);
}
