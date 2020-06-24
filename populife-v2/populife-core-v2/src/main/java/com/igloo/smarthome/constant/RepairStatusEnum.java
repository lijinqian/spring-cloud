/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.constant;

public enum RepairStatusEnum {
	
	cancelled(0, "已取消"), submitted(1, "已提交"), approved(3, "审核通过"), audit_fail(6, "审核未通过"), repairlocations_arrived(9, "到达维修点"), detected(12, "已检测"), user_agree(15, "用户已同意"), 
		repairing(18, "修理中"), repair_complete(21, "维修完成"), sent_back(24, "已寄回"), delivered(27, "已送达"), completed(30, "完成");
	
	private RepairStatusEnum(Integer code, String label) {
		this.code = code;
		this.label = label;
	}
	
	/**
	 * 是否已经审核
	 * @param status
	 * @return
	 */
	public static boolean isApproved(Integer status) {
		return status.equals(approved.code) || status.equals(audit_fail.code);
	}
	
	public static boolean isDetected(Integer status) {
		return status >= detected.code; 
	}
	
	public Integer code;
	
	public String label;
}
