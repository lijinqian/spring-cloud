/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.repair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.repair.RepairConclusionMapper;
import com.igloo.smarthome.model.repair.RepairConclusion;

/**
 * 
 * @author Ares S
 * @date 2020年6月13日
 */
@Service
public class RepairConclusionService {

	@Autowired
	RepairConclusionMapper repairConclusionMapper;
	
	public RepairConclusion getById(String applyNo) {
		return this.repairConclusionMapper.selectByPrimaryKey(applyNo);
	}
}
