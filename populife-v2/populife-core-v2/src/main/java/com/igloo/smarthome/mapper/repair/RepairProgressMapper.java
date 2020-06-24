/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.mapper.repair;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.igloo.smarthome.model.repair.RepairProgress;

import tk.mybatis.mapper.common.Mapper;

/**
 * 
 * @author Ares S
 * @date 2020年6月8日
 */
public interface RepairProgressMapper extends Mapper<RepairProgress> {
	
	List<RepairProgress> getRepairProgress(@Param("applyNo") String applyNo);
}
