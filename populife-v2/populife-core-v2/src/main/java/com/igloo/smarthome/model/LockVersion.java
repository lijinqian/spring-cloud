/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model;

import javax.persistence.Id;

/**
 * 锁版本
 * @author lijq
 * @Date 2018年8月21日
 */
public class LockVersion {
	
	@Id
	private Integer lockId;

	/** 协议类型 */
	private Integer protocolType;

	/** 协议版本 */
	private Integer protocolVersion;

	/** 场景 */
	private Integer scene;

	/** 公司 */
	private Integer groupId;

	/** 应用商 */
	private Integer orgId;
	
	/** 硬件版本号 */
	private String hardwareRevision;
	
	/** 固件版本号 */
	private String firmwareRevision;
	
	
	/** 暂留字段，后期可能有用 */
	private String logoUrl;
	private String showAdminKbpwdFlag;

	public Integer getLockId() {
		return lockId;
	}

	public void setLockId(Integer lockId) {
		this.lockId = lockId;
	}

	public Integer getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(Integer protocolType) {
		this.protocolType = protocolType;
	}

	public Integer getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(Integer protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public Integer getScene() {
		return scene;
	}

	public void setScene(Integer scene) {
		this.scene = scene;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public Integer getOrgId() {
		return orgId;
	}

	public void setOrgId(Integer orgId) {
		this.orgId = orgId;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getShowAdminKbpwdFlag() {
		return showAdminKbpwdFlag;
	}

	public void setShowAdminKbpwdFlag(String showAdminKbpwdFlag) {
		this.showAdminKbpwdFlag = showAdminKbpwdFlag;
	}

	public String getHardwareRevision() {
		return hardwareRevision;
	}

	public void setHardwareRevision(String hardwareRevision) {
		this.hardwareRevision = hardwareRevision;
	}

	public String getFirmwareRevision() {
		return firmwareRevision;
	}

	public void setFirmwareRevision(String firmwareRevision) {
		this.firmwareRevision = firmwareRevision;
	}
	
	
}
