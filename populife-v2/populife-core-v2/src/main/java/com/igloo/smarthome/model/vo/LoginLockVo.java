package com.igloo.smarthome.model.vo;

import java.util.Date;

import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.LockVersion;

public class LoginLockVo extends Lock{
	
	/**
	 * 钥匙用户类型：110301-管理员钥匙，110302-普通用户钥匙
	 */
	private String userType;
	
	/** 1限时，2永久，3单次，4循环 */
	private Integer keyType;
	
	/** 有效期开始时间 */
	private Long startDate;

	/** 有效期结束时间 */
	private Long endDate;
	
	/** 钥匙是否被授权：0-否，1-是 */
	private Integer keyRight;

	/** 钥匙状态（110401：正常使用，110402：待接收，110405：已冻结，110408：已删除，110410：已重置） */
	private String keyStatus;
	
	/** 锁分组名称 */
	String homeName;
	
	/** 锁的版本信息  */
	LockVersion lockVersion;
	
	/** 锁数量 */
	Integer lockNum;
	
	/** 是否允许远程开锁 */
	Boolean allowRemoteUnlock;

	public Integer getKeyType() {
		return keyType;
	}

	public void setKeyType(Integer keyType) {
		this.keyType = keyType;
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

	public String getKeyStatus() {
		if(keyType != null && keyType == Constants.KeyType.LIMITTIME ) {
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

	/**
	 * @return the homeName
	 */
	public String getHomeName() {
		return homeName;
	}

	/**
	 * @param homeName the homeName to set
	 */
	public void setHomeName(String homeName) {
		this.homeName = homeName;
	}

	public LockVersion getLockVersion() {
		return lockVersion;
	}

	public void setLockVersion(LockVersion lockVersion) {
		this.lockVersion = lockVersion;
	}

	public Integer getLockNum() {
		return lockNum;
	}

	public void setLockNum(Integer lockNum) {
		this.lockNum = lockNum;
	}

	public Boolean getAllowRemoteUnlock() {
		return allowRemoteUnlock;
	}

	public void setAllowRemoteUnlock(Boolean allowRemoteUnlock) {
		this.allowRemoteUnlock = allowRemoteUnlock;
	}
	

}
