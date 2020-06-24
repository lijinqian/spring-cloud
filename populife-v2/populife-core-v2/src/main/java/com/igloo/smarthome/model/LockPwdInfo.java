package com.igloo.smarthome.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 锁
 * @author lijq
 * @Date 2018年8月21日
 */
@Table(name = "`lock`")
public class LockPwdInfo {


	/** 锁id */
	@Id
	private Integer lockId;

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
	
	
	public Integer getLockId() {
		return lockId;
	}

	public void setLockId(Integer lockId) {
		this.lockId = lockId;
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
