package com.igloo.smarthome.model.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ExpireKeyVo{
	
	private String avatar;
	private String nickname;
	private Long startDate;
	private Long endDate;
	private String lockAlias;
	private Integer dayNum;
	private String keyStatus;
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
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
	public String getLockAlias() {
		return lockAlias;
	}
	public void setLockAlias(String lockAlias) {
		this.lockAlias = lockAlias;
	}
	public Integer getDayNum() {
		return dayNum;
	}
	public void setDayNum(Integer dayNum) {
		this.dayNum = dayNum;
	}
	
	public String getKeyStatus() {
		return keyStatus;
	}
	public void setKeyStatus(String keyStatus) {
		this.keyStatus = keyStatus;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	
	
	
}
