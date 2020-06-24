package com.igloo.smarthome.model.vo;

/**
 * 锁用户管理
 * @author lijq
 * @Date 2018年8月29日
 */
public class LockUserKeyVo {
	
	private Integer keyId;
	private String lockAlias;
	private String keyAlias;
	private Integer type;
	private Integer keyStatus;
	private String startDate;
	private String endDate;
	
	
	public Integer getKeyId() {
		return keyId;
	}
	public void setKeyId(Integer keyId) {
		this.keyId = keyId;
	}
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
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getKeyStatus() {
		return keyStatus;
	}
	public void setKeyStatus(Integer keyStatus) {
		this.keyStatus = keyStatus;
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
	
	
	
	
	
}
