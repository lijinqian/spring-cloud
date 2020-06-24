package com.igloo.smarthome.model.vo;

/**
 * 锁用户管理
 * @author lijq
 * @Date 2018年8月29日
 */
public class LockUserVo extends KeyDetailVo{
	
	private String avatar;
	private String userId;
	private String nickname;
	private String userName;
	
	
	public String getAvatar() {
		return avatar;
	}
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	
	
	
}
