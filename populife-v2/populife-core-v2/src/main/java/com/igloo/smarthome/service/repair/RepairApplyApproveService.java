/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.repair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.repair.RepairApplyApproveFailMapper;
import com.igloo.smarthome.mapper.repair.RepairApplyApproveMapper;
import com.igloo.smarthome.model.repair.RepairApplyApprove;
import com.igloo.smarthome.model.repair.RepairApplyApproveFail;

/**
 * 
 * @author Ares S
 * @date 2020年6月13日
 */
@Service
public class RepairApplyApproveService {
	
	@Autowired
	RepairApplyApproveMapper repairApplyApproveMapper;
	
	@Autowired
	RepairApplyApproveFailMapper repairApplyApproveFailMapper;

	public RepairApplyApprove getById(String applyNo) {
		return this.repairApplyApproveMapper.selectByPrimaryKey(applyNo);
	}
	
	public RepairApplyApproveFail getRepairApplyApproveFailById(String applyNo) {
		return this.repairApplyApproveFailMapper.selectByPrimaryKey(applyNo);
	}
}

