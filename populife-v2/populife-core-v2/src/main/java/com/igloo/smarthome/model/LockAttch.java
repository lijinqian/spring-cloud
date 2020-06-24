package com.igloo.smarthome.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 锁
 * @author lijq
 * @Date 2018年8月21日
 */
@Table(name = "`lock`")
public class LockAttch {


	/** 锁id */
	@Id
	private Integer lockId;
	
	/** 锁开门标志位 */
	private Integer flagPos;

	/** 时间戳，用于初始化密码数据，SDK提供 */
	private Long timestamp;

	/** 锁特征值，用于表示锁支持的功能 */
	private Integer specialValue;

	/** 锁电量 */
	private Integer electricQuantity;

	/** 锁所在时区和UTC时区时间的差数，单位milliseconds */
	private Long timezoneRawOffSet;

	/** 产品型号（用于锁固件升级） */
	private String modelNum;

	/** 是否支持远程开锁：1-是、2-否 */
	private Integer remoteEnable;
	
	public Integer getLockId() {
		return lockId;
	}

	public void setLockId(Integer lockId) {
		this.lockId = lockId;
	}

	public Integer getFlagPos() {
		return flagPos;
	}

	public void setFlagPos(Integer flagPos) {
		this.flagPos = flagPos;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getSpecialValue() {
		return specialValue;
	}

	public void setSpecialValue(Integer specialValue) {
		this.specialValue = specialValue;
	}

	public Integer getElectricQuantity() {
		return electricQuantity;
	}

	public void setElectricQuantity(Integer electricQuantity) {
		this.electricQuantity = electricQuantity;
	}

	public Long getTimezoneRawOffSet() {
		return timezoneRawOffSet;
	}

	public void setTimezoneRawOffSet(Long timezoneRawOffSet) {
		this.timezoneRawOffSet = timezoneRawOffSet;
	}

	public String getModelNum() {
		return modelNum;
	}

	public void setModelNum(String modelNum) {
		this.modelNum = modelNum;
	}

	public Integer getRemoteEnable() {
		return remoteEnable;
	}

	public void setRemoteEnable(Integer remoteEnable) {
		this.remoteEnable = remoteEnable;
	}

	
}
