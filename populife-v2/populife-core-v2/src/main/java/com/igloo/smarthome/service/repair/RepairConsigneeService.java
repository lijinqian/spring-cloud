/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.repair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.repair.RepairConsigneeMapper;
import com.igloo.smarthome.model.repair.RepairConsignee;

/**
 * 
 * @author Ares S
 * @date 2020年6月13日
 */
@Service
public class RepairConsigneeService {
	
	@Autowired
	RepairConsigneeMapper repairConsigneeMapper;
	
	public RepairConsignee getById(String applyNo) {
		return this.repairConsigneeMapper.selectByPrimaryKey(applyNo);
	}
}
