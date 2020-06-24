package com.igloo.smarthome.model.vo;

import javax.persistence.Column;

import com.igloo.smarthome.model.Lock;

public class LockInfoVo extends Lock{
	
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
	
	/** 锁开门的关键信息，开门用的 */
	@Column(name = "`key`")
	private String key;

	/** Aes加解密key */
	private String aesKey;

	/** 管理员钥匙会有，锁的管理员密码，锁管理相关操作需要携带，校验管理员权限 */
	private String adminPwd;

	/** 管理员键盘密码 */
	private String noKeyPwd;

	/** 二代锁的管理员钥匙会有，清空码 */
	private String deletePwd;

	/** 密码数据，用于生成密码，SDK提供 */
	private String pwdInfo;

	/** 键盘密码版本: 0、1、2、3、4 */
	private Integer keyboardPwdVersion;
	
	/** 锁数据，由SDK生成 v3 */
	private String lockData;

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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getAesKey() {
		return aesKey;
	}

	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}

	public String getAdminPwd() {
		return adminPwd;
	}

	public void setAdminPwd(String adminPwd) {
		this.adminPwd = adminPwd;
	}

	public String getNoKeyPwd() {
		return noKeyPwd;
	}

	public void setNoKeyPwd(String noKeyPwd) {
		this.noKeyPwd = noKeyPwd;
	}

	public String getDeletePwd() {
		return deletePwd;
	}

	public void setDeletePwd(String deletePwd) {
		this.deletePwd = deletePwd;
	}

	public String getPwdInfo() {
		return pwdInfo;
	}

	public void setPwdInfo(String pwdInfo) {
		this.pwdInfo = pwdInfo;
	}

	public Integer getKeyboardPwdVersion() {
		return keyboardPwdVersion;
	}

	public void setKeyboardPwdVersion(Integer keyboardPwdVersion) {
		this.keyboardPwdVersion = keyboardPwdVersion;
	}

	public String getLockData() {
		return lockData;
	}

	public void setLockData(String lockData) {
		this.lockData = lockData;
	}
	
	
}
