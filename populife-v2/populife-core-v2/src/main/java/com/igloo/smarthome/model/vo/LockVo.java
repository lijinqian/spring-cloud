package com.igloo.smarthome.model.vo;

import java.util.Date;

import com.igloo.smarthome.constant.Constants;

public class LockVo {
	
	private String lockAlias;
	private String keyAlias;
	private Integer electricQuantity;
	private Integer keyId;
	private Long startDate;
	private Long endDate;
	private String keyStatus;
	private Integer type;
	private String userType;
	private Integer keyRight;
	private Integer dayNum;
	public String getLockAlias() {
		return lockAlias;
	}
	public void setLockAlias(String lockAlias) {
		this.lockAlias = lockAlias;
	}
	public String getKeyAlias() {
		return keyAlias;
	}
	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}
	public Integer getKeyId() {
		return keyId;
	}
	public void setKeyId(Integer keyId) {
		this.keyId = keyId;
	}
	public Long getStartDate() {
		return startDate;
	}
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}
	public Long getEndDate() {
		return endDate;
	}
	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}
	public String getKeyStatus() {
		if(type != null && type == Constants.KeyType.LIMITTIME ) {
			Date t = new Date();
			if( startDate > t.getTime()/1000 ) {
			//未生效;
				return "110501";
			}
		}	
		return keyStatus;
	}
	
	public void setKeyStatus(String keyStatus) {
		this.keyStatus = keyStatus;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public Integer getKeyRight() {
		return keyRight;
	}
	public void setKeyRight(Integer keyRight) {
		this.keyRight = keyRight;
	}
	public Integer getDayNum() {
		return dayNum;
	}
	public void setDayNum(Integer dayNum) {
		this.dayNum = dayNum;
	}
	public Integer getElectricQuantity() {
		return electricQuantity;
	}
	public void setElectricQuantity(Integer electricQuantity) {
		this.electricQuantity = electricQuantity;
	}
	
	

}
