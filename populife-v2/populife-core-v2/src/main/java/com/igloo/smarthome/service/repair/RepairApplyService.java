/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service.repair;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.constant.RepairStatusEnum;
import com.igloo.smarthome.mapper.repair.RepairApplyLockMapper;
import com.igloo.smarthome.mapper.repair.RepairApplyMapper;
import com.igloo.smarthome.mapper.repair.RepairConsigneeMapper;
import com.igloo.smarthome.mapper.repair.RepairProgressMapper;
import com.igloo.smarthome.model.repair.RepairApply;
import com.igloo.smarthome.model.repair.RepairApplyLock;
import com.igloo.smarthome.model.repair.RepairConsignee;
import com.igloo.smarthome.model.repair.RepairProgress;

/**
 * 
 * @author Ares S
 * @date 2020年6月11日
 */
@Service
public class RepairApplyService {

	@Autowired
	RepairApplyMapper repairApplyMapper;
	
	@Autowired
	RepairConsigneeMapper repairConsigneeMapper;
	
	@Autowired
	RepairApplyLockMapper repairApplyLockMapper;
	
	@Autowired
	RepairProgressMapper repairProgressMapper;
	
	public RepairApply getUncompleteByLockId(Integer lockId) {
		return this.repairApplyMapper.getUncompleteByLockId(lockId);
	}
	
	public void addRepairApply(RepairApply repairApply, RepairConsignee repairConsignee, RepairApplyLock repairApplyLock) {
		this.repairApplyMapper.insertSelective(repairApply);
		this.repairConsigneeMapper.insertSelective(repairConsignee);
		this.repairApplyLockMapper.insertSelective(repairApplyLock);
		this.repairProgressMapper.insertSelective(new RepairProgress(repairApply.getApplyNo(), repairApply.getStatus(), "--", repairApply.getCreateDate()));
	}
	
	public List<RepairApply> getRepairApply(String userId, Integer start, Integer limit) {
		return this.repairApplyMapper.getRepairApply(userId, start, limit);
	}
	
	public RepairApply getById(String applyNo) {
		return this.repairApplyMapper.selectByPrimaryKey(applyNo);
	}
	
	public void updateStatus(String applyNo, Integer status, String remark) {
		this.repairApplyMapper.updateByPrimaryKeySelective(new RepairApply(applyNo, status));
		this.repairProgressMapper.insertSelective(new RepairProgress(applyNo, status, remark, new Date()));
	}
	
	public void cancelRepairApply(String applyNo, String remark) {
		RepairApply repairApply = new RepairApply();
		repairApply.setApplyNo(applyNo);
		repairApply.setCancelled(true);
		this.repairApplyMapper.updateByPrimaryKeySelective(repairApply);
		this.repairProgressMapper.insertSelective(new RepairProgress(applyNo, RepairStatusEnum.cancelled.code, remark, new Date()));
	}
}
