/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.model;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author shiwei
 * @date 2018年8月19日
 */
public class User {
	
	@Id
	String id;
	
	String phone, email, password, nickname, avatar, username, isDeleted, deviceId, apnsToken, optSystem;
	
	Integer curKeyId, accountType;
	
	@Transient
	Integer openid;
	
	/**
	 * @return the isDeleted
	 */
	public String getIsDeleted() {
		return isDeleted;
	}

	/**
	 * @param isDeleted the isDeleted to set
	 */
	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}


	Date registeredDate;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the password
	 */
	@JsonIgnore
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * @param nickname the nickname to set
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * @return the avatar
	 */
	public String getAvatar() {
		return avatar;
	}

	/**
	 * @param avatar the avatar to set
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	/**
	 * @return the registeredDate
	 */
	public Date getRegisteredDate() {
		return registeredDate;
	}

	/**
	 * @param registeredDate the registeredDate to set
	 */
	public void setRegisteredDate(Date registeredDate) {
		this.registeredDate = registeredDate;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the deviceId
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * @param deviceId the deviceId to set
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * @return the apnsToken
	 */
	public String getApnsToken() {
		return apnsToken;
	}

	/**
	 * @param apnsToken the apnsToken to set
	 */
	public void setApnsToken(String apnsToken) {
		this.apnsToken = apnsToken;
	}

	public Integer getCurKeyId() {
		return curKeyId;
	}

	public void setCurKeyId(Integer curKeyId) {
		this.curKeyId = curKeyId;
	}

	public String getOptSystem() {
		return optSystem;
	}

	public void setOptSystem(String optSystem) {
		this.optSystem = optSystem;
	}

	/**
	 * @return the accountType
	 */
	public Integer getAccountType() {
		return accountType;
	}

	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(Integer accountType) {
		this.accountType = accountType;
	}

	public Integer getOpenid() {
		return openid;
	}

	public void setOpenid(Integer openid) {
		this.openid = openid;
	}

	
	
}
