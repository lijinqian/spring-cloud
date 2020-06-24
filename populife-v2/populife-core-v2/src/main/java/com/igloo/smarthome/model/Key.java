package com.igloo.smarthome.model;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 钥匙
 * @author lijq
 * @Date 2018年8月21日
 */
@Table(name="`key`")
public class Key {
	
	/**
	 * 
	 */
	public Key() {
		super();
	}

	/**
	 * @param userId
	 * @param keyStatus
	 */
	public Key(String userId, String keyStatus, Integer lockId) {
		super();
		this.userId = userId;
		this.keyStatus = keyStatus;
		this.lockId = lockId;
	}

	/** 钥匙id */
	@Id
	private Integer keyId;

	/** 用户id */
	private String userId;

	/** 锁id */
	private Integer lockId;

	/** 钥匙所有者的openid */
	private Integer openid;

	/** 用户名 */
	private String username;

	/** 有效期开始时间 */
	private Long startDate;

	/** 有效期结束时间 */
	private Long endDate;

	/** 钥匙状态（110401：正常使用，110402：待接收，110405：已冻结，110408：已删除，110410：已重置, 110500:已过期）*/
	private String keyStatus;

	/** 钥匙是否被授权：0-否，1-是 */
	private Integer keyRight;

	/** 发送者 */
	private String senderId;

	/**  发送时间 */
	private Date sendDate;

	/** 别名 */
	private String alias;

	/** 1限时，2永久，3单次，4循环 */
	private Integer type;
	
	/** 钥匙用户类型：110301-管理员钥匙，110302-普通用户钥匙 */
	private String userType;
	
	/** 备注，留言 */
	private String remarks;
	
	/** 是否支持远程开锁 */
	private Boolean allowRemoteUnlock;
	
	/** N:正常，Y：不给前台返回数据 */
	private String isClear;
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getLockId() {
		return lockId;
	}

	public void setLockId(Integer lockId) {
		this.lockId = lockId;
	}

	public Integer getOpenid() {
		return openid;
	}

	public void setOpenid(Integer openid) {
		this.openid = openid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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
		return keyStatus;
	}

	public void setKeyStatus(String keyStatus) {
		this.keyStatus = keyStatus;
	}

	public Integer getKeyRight() {
		return keyRight;
	}

	public void setKeyRight(Integer keyRight) {
		this.keyRight = keyRight;
	}

	/**
	 * @return the senderId
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * @param senderId the senderId to set
	 */
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}


	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getKeyId() {
		return keyId;
	}

	public void setKeyId(Integer keyId) {
		this.keyId = keyId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getIsClear() {
		return isClear;
	}

	public void setIsClear(String isClear) {
		this.isClear = isClear;
	}

	public Boolean getAllowRemoteUnlock() {
		return allowRemoteUnlock;
	}

	public void setAllowRemoteUnlock(Boolean allowRemoteUnlock) {
		this.allowRemoteUnlock = allowRemoteUnlock;
	}
	
	
	
	
}
