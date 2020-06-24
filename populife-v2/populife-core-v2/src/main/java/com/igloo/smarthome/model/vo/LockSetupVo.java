package com.igloo.smarthome.model.vo;

/**
 * 
 * @author lijq
 * @Date 2018年9月1日
 */
public class LockSetupVo {
	
	private String userType;
	private Integer keyId;
	private Integer lockId;
	private String lockName;
	private String lockMac;
	private Integer electricQuantity;
	private Integer keyType;
	private String startDate;
	private String endDate;
	private String lockAlias;
	private String groupName;
	private String noKeyPwd;
	
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public Integer getKeyId() {
		return keyId;
	}
	public void setKeyId(Integer keyId) {
		this.keyId = keyId;
	}
	public Integer getLockId() {
		return lockId;
	}
	public void setLockId(Integer lockId) {
		this.lockId = lockId;
	}
	public String getLockName() {
		return lockName;
	}
	public void setLockName(String lockName) {
		this.lockName = lockName;
	}
	public String getLockMac() {
		return lockMac;
	}
	public void setLockMac(String lockMac) {
		this.lockMac = lockMac;
	}
	public Integer getElectricQuantity() {
		return electricQuantity;
	}
	public void setElectricQuantity(Integer electricQuantity) {
		this.electricQuantity = electricQuantity;
	}
	public Integer getKeyType() {
		return keyType;
	}
	public void setKeyType(Integer keyType) {
		this.keyType = keyType;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getLockAlias() {
		return lockAlias;
	}
	public void setLockAlias(String lockAlias) {
		this.lockAlias = lockAlias;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getNoKeyPwd() {
		return noKeyPwd;
	}
	public void setNoKeyPwd(String noKeyPwd) {
		this.noKeyPwd = noKeyPwd;
	}
	
	
	
}
