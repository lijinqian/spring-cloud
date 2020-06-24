/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.igloo.smarthome.model.OperationLog;
import com.igloo.smarthome.model.vo.OperationLogVo;

import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author shiwei
 * @date 2018年9月1日
 */
public interface OperationLogMapper extends Mapper<OperationLog> {
	
	List<OperationLogVo> getLog(@Param("lockId") Integer lockId, @Param("keyword") String keyword, @Param("start") Integer start, 
			@Param("limit") Integer limit, @Param("userId") String userId, @Param("userType") int userType);
	
	List<OperationLogVo> getLog4Key(@Param("keyId") Integer lockId, @Param("start") Integer start, @Param("limit") Integer limit);
	
	List<OperationLog> getLog4Password(@Param("password") String password, @Param("start") Integer start, @Param("limit") Integer limit);
	
	void batchInsert(List<OperationLog> logList);

	void delIccLog(@Param("cardNumber") String cardNumber);
	
	void removeAll(@Param("lockId") Integer lockId, @Param("userId") String userId, @Param("userType") int userType);
	
	List<OperationLogVo> getLogByLockId(@Param("lockId") Integer lockId);

}
	

