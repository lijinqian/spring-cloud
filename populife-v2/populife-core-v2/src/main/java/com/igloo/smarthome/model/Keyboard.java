package com.igloo.smarthome.model;

import java.util.Date;

import javax.persistence.Id;

/**
 * 键盘密码
 * @author lijq
 * @Date 2018年8月25日
 */
public class Keyboard {

	/** 键盘密码ID */
	@Id
	private Integer keyboardPwdId;

	/** 键盘密码 */
	private String keyboardPwd;

	/** 锁id */
	private Integer lockId;

	/** 键盘密码版本, 三代锁的密码版本为4 */
	private Integer keyboardPwdVersion;

	/** 键盘密码类型 */
	private Integer keyboardPwdType;

	/** 有效期开始时间 */
	private Long startDate;

	/** 有效期结束时间 */
	private Long endDate;

	/** 创建时间 */
	private Date createDate;
	
	/** 删除方式:0-未删除，1-通过APP走蓝牙删除，2-通过网关走WIFI删除；不传则默认1, 必需先通过APP蓝牙删除后调用该接口，如果锁有连接网关，则可以传2，直接调用该接口删除 */
	private Integer deleteType;
	
	/** 添加方式:1-通过APP走蓝牙添加，2-通过网关走WIFI添加；不传则默认1,必需先通过APP蓝牙添加后调用该接口，如果锁有连接网关，则可以传2，直接调用该接口添加 */
	private Integer addType;
	
	/** 首次使用时间 */
	private Date firstTime;
	
	/** 发送者 */
	private String senderId;
	
	/** 别名 */
	private String alias;
	
	/** 修改方式:1-通过APP走蓝牙修改，2-通过网关走WIFI修改；不传则默认1,必需先通过APP蓝牙修改后调用该接口，如果锁有连接网关，则可以传2，直接调用该接口修改密码 */
	private Integer changeType;
	
	/** 钥匙id */
	private Integer keyId;
	
	public Integer getKeyboardPwdId() {
		return keyboardPwdId;
	}

	public void setKeyboardPwdId(Integer keyboardPwdId) {
		this.keyboardPwdId = keyboardPwdId;
	}

	public String getKeyboardPwd() {
		return keyboardPwd;
	}

	public void setKeyboardPwd(String keyboardPwd) {
		this.keyboardPwd = keyboardPwd;
	}

	public Integer getLockId() {
		return lockId;
	}

	public void setLockId(Integer lockId) {
		this.lockId = lockId;
	}

	public Integer getKeyboardPwdVersion() {
		return keyboardPwdVersion;
	}

	public void setKeyboardPwdVersion(Integer keyboardPwdVersion) {
		this.keyboardPwdVersion = keyboardPwdVersion;
	}

	public Integer getKeyboardPwdType() {
		return keyboardPwdType;
	}

	public void setKeyboardPwdType(Integer keyboardPwdType) {
		this.keyboardPwdType = keyboardPwdType;
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

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Integer getDeleteType() {
		return deleteType;
	}

	public void setDeleteType(Integer deleteType) {
		this.deleteType = deleteType;
	}

	public Integer getAddType() {
		return addType;
	}

	public void setAddType(Integer addType) {
		this.addType = addType;
	}

	public Date getFirstTime() {
		return firstTime;
	}

	public void setFirstTime(Date firstTime) {
		this.firstTime = firstTime;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Integer getChangeType() {
		return changeType;
	}

	public void setChangeType(Integer changeType) {
		this.changeType = changeType;
	}

	public Integer getKeyId() {
		return keyId;
	}

	public void setKeyId(Integer keyId) {
		this.keyId = keyId;
	}
	
	

}
