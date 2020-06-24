package com.igloo.smarthome.model.vo;

import java.util.Date;

import com.igloo.smarthome.constant.Constants;

/**
 * 锁详情
 * @author lijq
 * @Date 2018年8月22日
 */
public class KeyDetailVo {
	
	private Integer keyId;
	private String alias;
	private Long startDate;
	private Long endDate;
	private String recUser;
	private String sendUser;
	private Date sendDate;
	private String keyStatus;
	private Integer type;
	private String avatar;
	private Integer keyRight;
	
	
	public Integer getKeyId() {
		return keyId;
	}
	public void setKeyId(Integer keyId) {
		this.keyId = keyId;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getRecUser() {
		return recUser;
	}
	public void setRecUser(String recUser) {
		this.recUser = recUser;
	}
	public String getSendUser() {
		return sendUser;
	}
	public void setSendUser(String sendUser) {
		this.sendUser = sendUser;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
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
	public Date getSendDate() {
		return sendDate;
	}
	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public Integer getKeyRight() {
		return keyRight;
	}
	public void setKeyRight(Integer keyRight) {
		this.keyRight = keyRight;
	}
	
	
	
}
