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
public class Lock {


	/** 锁id */
	@Id
	private Integer lockId;
	
	/** 用户id */
	private String userId;

	/** 锁的蓝牙名称 */
	@Column(name = "`name`")
	private String name;

	/** 锁别名 */
	private String alias;

	/** 锁mac地址 */
	private String mac;


	/** 初始化时间 */
	private Date initDate;
	
	/** 管理员钥匙id */
	private Integer keyId;
	
	/** 0 删除，1 正常 */
	private Integer status;
	
	
	public Integer getLockId() {
		return lockId;
	}

	public void setLockId(Integer lockId) {
		this.lockId = lockId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public Date getInitDate() {
		return initDate;
	}

	public void setInitDate(Date initDate) {
		this.initDate = initDate;
	}

	public Integer getKeyId() {
		return keyId;
	}

	public void setKeyId(Integer keyId) {
		this.keyId = keyId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	

	
	
	
}
