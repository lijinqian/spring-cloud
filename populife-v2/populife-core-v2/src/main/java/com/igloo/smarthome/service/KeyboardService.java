/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.ext.ScienerService;
import com.igloo.smarthome.mapper.KeyboardMapper;
import com.igloo.smarthome.mapper.LockAttchMapper;
import com.igloo.smarthome.mapper.LockMapper;
import com.igloo.smarthome.mapper.LockPwdInfoMapper;
import com.igloo.smarthome.model.Keyboard;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.LockAttch;
import com.igloo.smarthome.model.LockPwdInfo;
import com.igloo.smarthome.model.User;
import com.igloo.smarthome.model.vo.KeyboardPwdVo;

import tcsyn.basic.ext.SystemException;
import tcsyn.basic.model.ExceptionCode;
import tcsyn.basic.util.DateUtil;
import tcsyn.basic.util.HttpClientUtil;
import tcsyn.basic.util.JsonUtil;
import tk.mybatis.mapper.entity.Example;

/**
 * 
 * @author lijq
 * @Date 2018年8月25日
 */
@Service
public class KeyboardService {
	
	final String dateFormat = "yyyy-MM-dd HH:mm";
	
	@Autowired
	ScienerService scienerService;
	
	@Autowired
	KeyboardMapper keyboardMapper;
	
	@Autowired
	LockMapper lockMapper;
	
	@Autowired
	LockAttchMapper lockAttchMapper;
	
	@Autowired
	LockPwdInfoMapper lockPwdInfoMapper;
	
	@Value("${sciener.appid}")
	String appid;
	
	@Autowired
	KeyboardService keyboardService;
	

	/**
	 * 获取键盘密码
	 * @param user
	 * @param lock
	 * @param keyboardPwdVersion
	 * @param keyboardPwdType
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws ParseException 
	 */
	@Transactional
	public Map<String, Object> get(User user, Lock lock, Integer keyboardPwdVersion, Integer keyboardPwdType,
			String startDate, String endDate, Integer timeZone, Integer keyId, String alias) throws ParseException {
		
		Keyboard keyboard = new Keyboard();
		keyboard.setLockId(lock.getLockId());
		keyboard.setKeyboardPwdVersion(keyboardPwdVersion);
		keyboard.setKeyboardPwdType(keyboardPwdType);
		keyboard.setCreateDate(new Date());
		keyboard.setSenderId(user.getId());
		keyboard.setKeyId(keyId);
		keyboard.setAlias(alias);
		long startCurrentTimeMillis = System.currentTimeMillis();
		if(StringUtils.isNoneBlank(startDate)){
			Long startTime = DateUtil.getDateTime(startDate, dateFormat, timeZone).getTime();
			keyboard.setStartDate(startTime);
		}else{
			keyboard.setStartDate(startCurrentTimeMillis);
		}
		
		//单次的有效期是6小时
		if(Constants.KeyboardPwdType.ONE == keyboardPwdType){
			keyboard.setEndDate(DateUtils.addHours(new Date(startCurrentTimeMillis), 6).getTime());
		}else{
			if(StringUtils.isNoneBlank(endDate)){
				Long endTime = DateUtil.getDateTime(endDate, dateFormat, timeZone).getTime();
				keyboard.setEndDate(endTime);
			}
		}
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("lockId", String.valueOf(lock.getLockId()));
		paramMap.put("keyboardPwdVersion", String.valueOf(keyboardPwdVersion));
		paramMap.put("keyboardPwdType", String.valueOf(keyboardPwdType));
		if (null != keyboard.getEndDate()) {
			paramMap.put("startDate", String.valueOf(keyboard.getStartDate()));
			paramMap.put("endDate", String.valueOf(keyboard.getEndDate()));
		} else {
			paramMap.put("startDate", String.valueOf(keyboard.getStartDate()));
		}
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "keyboardPwd/get", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			String keyboardPwd = MapUtils.getString(resultMap, "keyboardPwd");
			Integer keyboardPwdId = MapUtils.getInteger(resultMap, "keyboardPwdId");
			if(null == keyboardPwdId){
				throw new IllegalStateException(result);
			}
			
			Integer count  = keyboardService.checkUsedKeyboards(lock.getLockId(), keyboardPwd);
			
			if( count > 0 ) {
//				return super.fail("This keyboard password of this lock is existed",ExceptionCode.BX1);
				return new HashMap<String,Object>();
			}
			
			keyboard.setKeyboardPwd(keyboardPwd);
			keyboard.setKeyboardPwdId(keyboardPwdId);
			keyboardMapper.insertSelective(keyboard);
			
			return resultMap;
			
		} catch (Exception e) {
			throw new SystemException("Failed to get keyboard password, please try again later", e);
		}
		
	}
	
	public Keyboard getKeyboardPwd(Integer keyboardPwdId) {
		return keyboardMapper.selectByPrimaryKey(keyboardPwdId);
	}

	@Transactional
	public void delete(User user, Integer lockId, Integer keyboardPwdId, Integer mediumType) {
		Keyboard keyboard = new Keyboard();
		keyboard.setKeyboardPwdId(keyboardPwdId);
		keyboard.setLockId(lockId);
		keyboard.setDeleteType(mediumType);
		keyboardMapper.updateByPrimaryKeySelective(keyboard);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("lockId", String.valueOf(lockId));
		paramMap.put("keyboardPwdId", String.valueOf(keyboardPwdId));
		paramMap.put("deleteType", String.valueOf(mediumType));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "keyboardPwd/delete", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errcode = MapUtils.getInteger(resultMap, "errcode");
			if(0 != errcode){
				throw new IllegalStateException(result);
			}
			
		} catch (Exception e) {
			throw new SystemException("Failed to delete keyboard password, please try again later", e);
		}
		
	}
	

	@Transactional
	public Integer add(User user, Integer lockId, String keyboardPwd, String startDate, String endDate, Integer timeZone, Integer keyId, String alias, Integer mediumType) throws ParseException {
		
		Keyboard keyboard = new Keyboard();
		keyboard.setLockId(lockId);
		keyboard.setKeyboardPwd(keyboardPwd);
		keyboard.setKeyId(keyId);
		keyboard.setKeyboardPwdType(Constants.KeyboardPwdType.DEADLINE);
		keyboard.setStartDate(DateUtil.getDateTime(startDate, dateFormat, timeZone).getTime());
		keyboard.setEndDate(DateUtil.getDateTime(endDate, dateFormat, timeZone).getTime());
		keyboard.setAddType(mediumType);
		keyboard.setSenderId(user.getId());
		keyboard.setCreateDate(new Date());
		keyboard.setAlias(alias);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("lockId", String.valueOf(lockId));
		paramMap.put("keyboardPwd", keyboardPwd);
		paramMap.put("startDate", String.valueOf(keyboard.getStartDate()));
		paramMap.put("endDate", String.valueOf(keyboard.getEndDate()));
		paramMap.put("addType", String.valueOf(mediumType));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "keyboardPwd/add", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer keyboardPwdId = MapUtils.getInteger(resultMap, "keyboardPwdId");
			if(null == keyboardPwdId){
				throw new IllegalStateException(result);
			}
			keyboard.setKeyboardPwdId(keyboardPwdId);
			keyboardMapper.insertSelective(keyboard);
			
			return keyboardPwdId;
			
		} catch (Exception e) {
			throw new SystemException("Failed to add keyboard password, please try again later", e);
		}
	}
	

	public List<KeyboardPwdVo> list(String userId, Boolean isAdmin, Integer lockId,  Integer start, Integer pageSize) {
		return keyboardMapper.list(userId, isAdmin, lockId, start, pageSize);
	}

	@Transactional
	public void resetKeyboardPwd(String userId, Integer lockId, String pwdInfo, Long timestamp) {
		//删除该锁的所有密码
		this.keyboardMapper.delKeyboardByLockId(Constants.KeyboardPwdDeleteType.BLUETOOTH, lockId);
		
		//更新密码数据和时间戳
		LockAttch lockAttch = new LockAttch();
		lockAttch.setLockId(lockId);
		lockAttch.setTimestamp(timestamp);
		this.lockAttchMapper.updateByPrimaryKeySelective(lockAttch);
		
		LockPwdInfo lockPwdInfo = new LockPwdInfo();
		lockPwdInfo.setLockId(lockId);
		lockPwdInfo.setPwdInfo(pwdInfo);
		this.lockPwdInfoMapper.updateByPrimaryKeySelective(lockPwdInfo);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(userId));
		paramMap.put("lockId", String.valueOf(lockId));
		paramMap.put("pwdInfo", pwdInfo);
		paramMap.put("timestamp", String.valueOf(timestamp));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/resetKeyboardPwd", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errcode = MapUtils.getInteger(resultMap, "errcode");
			if(0 != errcode){
				throw new IllegalStateException(result);
			}
			
		} catch (Exception e) {
			throw new SystemException("Failed to reset keyboard password, please try again later", e);
		}
		
	}

	@Transactional
	public void change(User user, Integer lockId, Integer keyboardPwdId, String newKeyboardPwd, Integer mediumType,
			String startDate, String endDate, Integer timeZone) throws ParseException {
		
		Keyboard keyboard = keyboardMapper.selectByPrimaryKey(keyboardPwdId);
		keyboard.setKeyboardPwdId(keyboardPwdId);
		keyboard.setLockId(lockId);
		Integer keyboardPwdType = keyboard.getKeyboardPwdType();
		//永久密码修改为限期密码
		if(Constants.KeyboardPwdType.PERMANENT == keyboardPwdType
				&& StringUtils.isNoneBlank(startDate, endDate)){
			keyboard.setKeyboardPwdType(Constants.KeyboardPwdType.DEADLINE);
		}
		if (StringUtils.isNotBlank(startDate)) {
			keyboard.setStartDate(DateUtil.getDateTime(startDate, dateFormat, timeZone).getTime());
		}
		if (StringUtils.isNotBlank(endDate)) {
			keyboard.setEndDate(DateUtil.getDateTime(endDate, dateFormat, timeZone).getTime());
		}
		keyboard.setChangeType(mediumType);
		keyboard.setKeyboardPwd(newKeyboardPwd);
		keyboardMapper.updateByPrimaryKeySelective(keyboard);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("lockId", String.valueOf(lockId));
		paramMap.put("keyboardPwdId", String.valueOf(keyboardPwdId));
		paramMap.put("newKeyboardPwd", newKeyboardPwd);
		if (keyboard.getStartDate() != null) {
			paramMap.put("startDate", String.valueOf(keyboard.getStartDate()));
		}
		if (keyboard.getEndDate() != null) {
			paramMap.put("endDate", String.valueOf(keyboard.getEndDate()));
		}
		paramMap.put("changeType", String.valueOf(mediumType));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "keyboardPwd/change", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errcode = MapUtils.getInteger(resultMap, "errcode");
			if(0 != errcode){
				throw new IllegalStateException(result);
			}
			
		} catch (Exception e) {
			throw new SystemException("Failed to modify keyboard password, please try again later", e);
		}
	}
	
	@Transactional
	public void changeByGateway(User user, Integer lockId, Integer keyboardPwdId, String newKeyboardPwd, Integer changeType,
			String startDate, String endDate, Integer timeZone) throws ParseException {
		
		Keyboard keyboard = keyboardMapper.selectByPrimaryKey(keyboardPwdId);
		String oldPassword = keyboard.getKeyboardPwd();
		keyboard.setKeyboardPwdId(keyboardPwdId);
		keyboard.setLockId(lockId);
		Integer keyboardPwdType = keyboard.getKeyboardPwdType();
		//永久密码修改为限期密码
		if(Constants.KeyboardPwdType.PERMANENT == keyboardPwdType
				&& StringUtils.isNoneBlank(startDate, endDate)){
			keyboard.setKeyboardPwdType(Constants.KeyboardPwdType.DEADLINE);
		}
		if (StringUtils.isNotBlank(startDate)) {
			keyboard.setStartDate(DateUtil.getDateTime(startDate, dateFormat, timeZone).getTime());
		}
		if (StringUtils.isNotBlank(endDate)) {
			keyboard.setEndDate(DateUtil.getDateTime(endDate, dateFormat, timeZone).getTime());
		}
		keyboard.setChangeType(changeType);
		keyboard.setKeyboardPwd(newKeyboardPwd);
		keyboardMapper.updateByPrimaryKeySelective(keyboard);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("lockId", String.valueOf(lockId));
		paramMap.put("keyboardPwdId", String.valueOf(keyboardPwdId));
		paramMap.put("newKeyboardPwd", newKeyboardPwd);
		if (StringUtils.isBlank(newKeyboardPwd)) {
			paramMap.put("newKeyboardPwd", oldPassword);
		} else {
			paramMap.put("newKeyboardPwd", newKeyboardPwd);
		}
		
		if (keyboard.getStartDate() != null) {
			paramMap.put("startDate", String.valueOf(keyboard.getStartDate()));
		}
		if (keyboard.getEndDate() != null) {
			paramMap.put("endDate", String.valueOf(keyboard.getEndDate()));
		}
		paramMap.put("changeType", String.valueOf(changeType));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "keyboardPwd/change", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errcode = MapUtils.getInteger(resultMap, "errcode");
			if(0 != errcode){
				throw new IllegalStateException(result);
			}
			
		} catch (Exception e) {
			throw new SystemException("Failed to modify keyboard password, please try again later", e);
		}
	}

	public void changeAlias(Integer keyboardPwdId, String alias) {
		Keyboard keyboard = new Keyboard();
		keyboard.setKeyboardPwdId(keyboardPwdId);
		keyboard.setAlias(alias);
		keyboardMapper.updateByPrimaryKeySelective(keyboard);
	}

	public void deleteUsedKeyboard(Integer lockId, Integer deleteType) {
		keyboardMapper.deleteUsedKeyboard(lockId, deleteType);
		
	}

	public Integer getUsedKeyboards(Integer lockId, String password) {
		return keyboardMapper.getUsedKeyboards(lockId, password);
	}
	
	public Integer checkUsedKeyboards(Integer lockId, String password) {
		return keyboardMapper.checkUsedKeyboards(lockId, password);
	}
	
}
