/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.repair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.mapper.repair.RepairLocationsMapper;
import com.igloo.smarthome.model.repair.RepairLocations;

/**
 * 
 * @author Ares S
 * @date 2020年6月11日
 */
@Service
public class RepairLocationsService {

	@Autowired
	RepairLocationsMapper repairLocationsMapper;
	
	public RepairLocations getRepairLocations(String countryCode) {
		return this.repairLocationsMapper.selectOne(new RepairLocations(countryCode));
	}
	
}
