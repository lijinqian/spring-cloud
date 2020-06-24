/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.igloo.smarthome.model.ErrorMsg;
import com.igloo.smarthome.model.vo.ErrorMsgVo;

import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author lijq
 * @Date 2018年8月25日
 */
public interface ErrorMsgMapper extends Mapper<ErrorMsg> {

	List<ErrorMsgVo> list(@Param("userid") String userid);

	void insertMsg(@Param("userid") String userid, @Param("msg") String msg);
}
