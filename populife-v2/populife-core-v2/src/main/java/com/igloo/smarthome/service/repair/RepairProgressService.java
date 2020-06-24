/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.repair;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.repair.RepairProgressMapper;
import com.igloo.smarthome.model.repair.RepairProgress;

/**
 * 
 * @author Ares S
 * @date 2020年6月13日
 */
@Service
public class RepairProgressService {

	@Autowired
	RepairProgressMapper repairProgressMapper;
	
	public List<RepairProgress> getRepairProgress(String applyNo) {
		return this.repairProgressMapper.getRepairProgress(applyNo);
	}
}
