/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.igloo.smarthome.model.Icc;

import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author shiwei
 * @date 2018年9月11日
 */
public interface IccMapper extends Mapper<Icc> {
	
	List<Icc> getIcc(@Param("lockId") Integer lockId, @Param("start") Integer start, @Param("limit") Integer limit, @Param("keyword") String keyword);
}
