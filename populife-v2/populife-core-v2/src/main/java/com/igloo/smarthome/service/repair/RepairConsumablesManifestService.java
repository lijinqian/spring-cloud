/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.repair;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.repair.RepairConsumablesManifestMapper;
import com.igloo.smarthome.model.repair.RepairConsumablesManifest;

/**
 * 
 * @author Ares S
 * @date 2020年6月13日
 */
@Service
public class RepairConsumablesManifestService {

	@Autowired
	RepairConsumablesManifestMapper repairConsumablesManifestMapper;
	
	public List<RepairConsumablesManifest> getByApplyNo(String applyNo) {
		return this.repairConsumablesManifestMapper.select(new RepairConsumablesManifest(applyNo));
	}
}
