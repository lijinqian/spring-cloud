/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.ext.PushService;
import com.igloo.smarthome.mapper.KeyMapper;
import com.igloo.smarthome.mapper.KeyboardMapper;
import com.igloo.smarthome.mapper.LockAttchMapper;
import com.igloo.smarthome.mapper.LockMapper;
import com.igloo.smarthome.mapper.LockPwdInfoMapper;
import com.igloo.smarthome.mapper.LockVersionMapper;
import com.igloo.smarthome.mapper.UserLockMapper;
import com.igloo.smarthome.mapper.UserMapper;
import com.igloo.smarthome.model.Key;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.LockAttch;
import com.igloo.smarthome.model.LockPwdInfo;
import com.igloo.smarthome.model.LockVersion;
import com.igloo.smarthome.model.User;
import com.igloo.smarthome.model.UserLock;
import com.igloo.smarthome.model.vo.KeyDetailVo;

import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.ext.SystemException;
import tcsyn.basic.util.DateUtil;
import tcsyn.basic.util.HttpClientUtil;
import tcsyn.basic.util.JsonUtil;
import tk.mybatis.mapper.entity.Example;

/**
 * 
 * @author lijq
 * @Date 2018年8月21日
 */
@Service
public class KeyService extends BaseService{
	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	LockMapper lockMapper;
	
	@Autowired
	LockAttchMapper lockAttchMapper;
	
	@Autowired
	LockPwdInfoMapper lockPwdInfoMapper;
	
	@Autowired
	KeyMapper keyMapper;
	
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	UserMessageService userMessageService;
	
	@Autowired
	UserLockMapper userLockMapper;
	
	@Autowired
	PushService pushService;
	
	@Autowired
	LockVersionMapper lockVersionMapper;
	
	@Autowired
	KeyboardMapper keyboardMapper;
	
	@Autowired
	OperationLogService operationLogService;
	
	@Autowired
	UserMapper userMapper;
	
	/**
	 * 发送
	 * @param sendUser
	 * @param recUser
	 * @param lockId
	 * @param type
	 * @param keyAlias
	 * @param timeJson
	 * @throws Exception
	 */
	@Transactional
	public void send(User sendUser, User recUser, Lock lock, Integer type, String keyAlias, 
			String startDate, String endDate, Integer timeZone, Boolean arUnlock, Boolean auAdmin) throws Exception {
		Integer lockId = lock.getLockId();
		Key key = new Key();
		key.setAlias(keyAlias);
		key.setType(type);
		key.setSenderId(sendUser.getId());
		key.setSendDate(new Date());
		key.setLockId(lockId);
		key.setUserId(recUser.getId());
		key.setUserType(Constants.KeyUserType.USER);
		key.setAllowRemoteUnlock(arUnlock);
		
		insertByType(key, type, startDate, endDate, timeZone);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(sendUser.getId()));
		paramMap.put("lockId", String.valueOf(lock.getLockId()));
		paramMap.put("receiverUsername", recUser.getUsername());
		paramMap.put("startDate", String.valueOf(key.getStartDate()));
		paramMap.put("endDate", String.valueOf(key.getEndDate()));
		paramMap.put("remarks", "");
		paramMap.put("remoteEnable", "2");
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "key/send", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			Integer kId = MapUtils.getInteger(resultMap, "keyId");
			
			Key key2 = new Key();
			key2.setKeyStatus(Constants.KeyStatus.DELETED);
			//把同一个用户同一个锁的钥匙置为删除
			Example keyExample = new Example(Key.class);
			keyExample.createCriteria().andEqualTo("lockId", lockId).andEqualTo("userId", recUser.getId());
			keyMapper.updateByExampleSelective(key2, keyExample);
			
			key.setKeyId(kId);
			key.setKeyStatus(Constants.KeyStatus.WATINGRECEIVE);
			keyMapper.insertSelective(key);
			String suffixMsg = "";
			if (type == 1) {
				suffixMsg = AbstractController.getText("key.auth.title", startDate, endDate);
			}
			
			this.userMessageService.addUserMessage(recUser.getId(), AbstractController.getText("key.sent.title"), AbstractController.getText("key.sent.content", key.getAlias()) + suffixMsg, null);
			
			// 推送通知
			Integer count = this.keyMapper.getNormalKeyCount(recUser.getId());
			int event = 2;
			if (count >= 2) {
				// app从主界面切换到锁列表
				event = 8;
			}
			this.pushService.send(recUser, AbstractController.getText("key.sent.content", key.getAlias()) + suffixMsg, event);
			
			//授权
			if(auAdmin){
				this.authorize(sendUser, key);
			}
			
		} catch (Exception e) {
			throw new SystemException("Failed to send the key, please try again later", e);
		}
	}
	
	public void insertByType(Key key, Integer type, String startDate, String endDate, Integer timeZone) throws Exception{
		final String dateFormat = "yyyy-MM-dd HH:mm";
		switch (type) {
		case 1: //1限时
			key.setStartDate(DateUtil.getDateTime(startDate, dateFormat, timeZone).getTime()/1000);
			key.setEndDate(DateUtil.getDateTime(endDate, dateFormat, timeZone).getTime()/1000);
//			key.setStartDate(DateUtils.parseDate(startDate, dateFormat).getTime() / 1000);
//			key.setEndDate(DateUtils.parseDate(endDate, dateFormat).getTime() / 1000);
			break;
		case 2: //2永久
			key.setStartDate(0L);
			key.setEndDate(0L);
			
			break;
		case 3: //3单次
			key.setStartDate(0L);
			key.setEndDate(0L);
			
			break;
		default:
			return;
		}
	}
	
	
	/**
	 * 删除
	 * @param user
	 * @param key
	 */
	@Transactional
	public void del(User user, Key key, String delType) {
		
		Integer keyId = key.getKeyId();
		if (StringUtils.equals(Constants.YES, delType) && Constants.KeyRight.YES == key.getKeyRight()) {
			// 删除授权用户所授权的用户的钥匙
			List<String> keyStatus = new ArrayList<String>();
			keyStatus.add(Constants.KeyStatus.NORMAL);
			keyStatus.add(Constants.KeyStatus.WATINGRECEIVE);
			keyStatus.add(Constants.KeyStatus.FROZEN);
			Example example = new Example(Key.class);
			example.createCriteria().andEqualTo("senderId", key.getUserId())
			.andIn("keyStatus", keyStatus);
			List<Key> keys = keyMapper.selectByExample(example);
			for(Key kk : keys){
				this.deleteOneKey(user.getId(), kk.getUserId(), kk.getKeyId());
				
				this.userLockMapper.delete(new UserLock(kk.getUserId(), kk.getLockId()));
			}
		} 
		// 删除授权用户或普通用户的钥匙
		this.userLockMapper.delete(new UserLock(key.getUserId(), key.getLockId()));
		
		//删除操作记录
		operationLogService.deleteByKeyId(key.getKeyId());
		
		//删除授权钥匙，可以选择删除或不删除他所发送的钥匙
		//普通用户只删除他自己的钥匙
	//			keyMapper.updateKeyStatusByLockIdAndSender(Constants.KeyStatus.DELETED, key.getLockId(), user.getId());
		
		key.setKeyStatus(Constants.KeyStatus.DELETED);
		keyMapper.updateByPrimaryKeySelective(key);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("keyId", String.valueOf(keyId));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "key/delete", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			
			userMessageService.addUserMessage(key.getUserId(), AbstractController.getText("key.deleted.title"), AbstractController.getText("key.deleted.content", key.getAlias()), 7);
		} catch (Exception e) {
			throw new SystemException("Failed to delete the key, please try again later", e);
		}
	}
	
	/**
	 * 
	 * @param userId
	 * @param keyId
	 */
	@Transactional
	public void deleteOneKey(String operator, String userId, Integer keyId) {
		
		Key key  = new Key();
		key.setKeyId(keyId);
		key.setKeyStatus(Constants.KeyStatus.DELETED);
		keyMapper.updateByPrimaryKeySelective(key);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(userId));
		paramMap.put("keyId", keyId.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "key/delete", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
//			operationLogService.addLog(operator, key.getLockId(), "Delete key with app", 7, keyId);
			userMessageService.addUserMessage(userId, AbstractController.getText("key.deleted.title"), AbstractController.getText("key.deleted.content", ""), 7);
			
			//删除操作记录
			operationLogService.deleteByKeyId(key.getKeyId());
			
		} catch (Exception e) {
			throw new SystemException("Failed to delete the key, please try again later", e);
		}
	}
	
	public Key getKeyById(Integer keyId) {
		return keyMapper.selectByPrimaryKey(keyId);
	}
	
	/**
	 * 冻结
	 * @param user
	 * @param key
	 */
	@Transactional
	public void freeze(User user, Key key) {
		key.setKeyStatus(Constants.KeyStatus.FROZEN);
		keyMapper.updateByPrimaryKeySelective(key);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("keyId", String.valueOf(key.getKeyId()));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "key/freeze", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			
			this.userMessageService.addUserMessage(key.getUserId(), AbstractController.getText("key.freeze.title"), AbstractController.getText("key.freeze.content", key.getAlias()), 3);
		} catch (Exception e) {
			throw new SystemException("Failed to freeze the key, please try again later", e);
		}
	}
	
	/**
	 * 解冻
	 * @param user
	 * @param key
	 */
	@Transactional
	public void unfreeze(User user, Key key) {
		key.setKeyStatus(Constants.KeyStatus.NORMAL);
		keyMapper.updateByPrimaryKeySelective(key);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("keyId", String.valueOf(key.getKeyId()));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "key/unfreeze", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			this.userMessageService.addUserMessage(key.getUserId(), AbstractController.getText("key.unfreeze.title"), AbstractController.getText("key.unfreeze.content", key.getAlias()), 4);
		} catch (Exception e) {
			throw new SystemException("Thawing the key failed, please try again later", e);
		}
		
	}
	
	/**
	 * 修改钥匙有效期
	 * @param user
	 * @param key
	 * @param timeJson
	 * @throws Exception
	 */
	@Transactional
	public void changePeriod(User user, Key key, Integer type, String startDate, String endDate, Integer timeZone) throws Exception {
		key.setType(type);
		insertByType(key, type, startDate, endDate, timeZone);
		keyMapper.updateByPrimaryKeySelective(key);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("keyId", String.valueOf(key.getKeyId()));
		paramMap.put("startDate", String.valueOf(key.getStartDate()));
		paramMap.put("endDate", String.valueOf(key.getEndDate()));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "key/changePeriod", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			User keyOwnerUser =  this.userMapper.selectByPrimaryKey(key.getUserId());
			this.pushService.send(keyOwnerUser,"The key period is changed.",110);
		} catch (Exception e) {
			throw new SystemException("The modification key validity period failed. Please try again later.", e);
		}
	}

	@Transactional
	public void authorize(User user, Key key) {
		key.setKeyRight(Constants.KeyRight.YES); //授权
		keyMapper.updateByPrimaryKeySelective(key);
		
		Integer lockId = key.getLockId();
		String userId = user.getId();
		// 钥匙授权其实是锁授权，被授权的用户拥有修改锁的参数和发送钥匙、发送密码的权限
		UserLock userLock = new UserLock(key.getUserId(), lockId);
		UserLock ul = this.userLockMapper.selectByPrimaryKey(userLock);
		if(null == ul){
			this.userLockMapper.insertSelective(userLock);
		}
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(userId));
		paramMap.put("lockId", String.valueOf(lockId));
		paramMap.put("keyId", String.valueOf(key.getKeyId()));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "key/authorize", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			userMessageService.addUserMessage(key.getUserId(), AbstractController.getText("key.auth.title"), AbstractController.getText("key.auth.content"), 5);
		} catch (Exception e) {
			throw new SystemException("Key authorization failed, please try again later", e);
		}
		
	}

	@Transactional
	public void unauthorize(User user, Key key) {
		key.setKeyRight(0); //解除授权
		keyMapper.updateByPrimaryKeySelective(key);
		this.userLockMapper.delete(new UserLock(key.getUserId(), key.getLockId()));
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("lockId", String.valueOf(key.getLockId()));
		paramMap.put("keyId", String.valueOf(key.getKeyId()));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "key/unauthorize", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			userMessageService.addUserMessage(key.getUserId(), AbstractController.getText("key.unauth.title"), AbstractController.getText("key.unauth.content"), 6);
		} catch (Exception e) {
			throw new SystemException("Unauthorization failed, please try again later", e);
		}
	}

	public KeyDetailVo getKeyDetail(Integer keyId) {
		KeyDetailVo vo = this.keyMapper.getKeyDetail(keyId);
		if(Constants.KeyType.LIMITTIME == vo.getType()){
			if(vo.getEndDate() < System.currentTimeMillis()){
				vo.setKeyStatus(Constants.KeyStatus.EXPIRE);
			}
		}
		return vo;
	}

	public List<KeyDetailVo> list(String userId, Integer lockId,Integer start, Integer pageSize, boolean isAdministrator) {
		return this.keyMapper.list(userId, lockId, start, pageSize, isAdministrator);
	}

	public void updateKeyById(Key key) {
		this.keyMapper.updateByPrimaryKeySelective(key);
		
	}

	@SuppressWarnings("unchecked")
	@Async
	public void syncData(String userId) {
		String updateKey = userId + "." + Constants.RedisKey.LASTUPDATEDATE;
		ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
		String lastTime = opsForValue.get(updateKey);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(userId));
		if(StringUtils.isNoneBlank(lastTime)){
			paramMap.put("lastUpdateDate", lastTime);
		}
		paramMap.put("date", String.valueOf(new Date().getTime()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "key/syncData", paramMap);
			logger.info("同步返回数据==>" + result);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			String lastUpdateDate = MapUtils.getString(resultMap, "lastUpdateDate");
			if(StringUtils.isBlank(lastUpdateDate)){
				throw new IllegalStateException(result);
			}
			List<Map<String, Object>> dataList = (List<Map<String, Object>>) resultMap.get("keyList");
			for(Map<String, Object> dataMap : dataList){
				//存在就更新，不存在就插入
				this.saveOrUpdateKey(dataMap, userId);
//				this.saveOrUpdateLock(dataMap);
			}
			opsForValue.set(updateKey, lastUpdateDate);
		} catch (Exception e) {
			throw new SystemException("Synchronization key data failed, please try again later", e);
		}
		
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private void saveOrUpdateLock(Map<String, Object> dataMap) {
		Lock lock = new Lock();
		Integer lockId = MapUtils.getInteger(dataMap, "lockId");
		lock.setLockId(lockId);
		lock.setName(MapUtils.getString(dataMap, "lockName"));
		lock.setAlias(MapUtils.getString(dataMap, "lockAlias"));
		lock.setMac(MapUtils.getString(dataMap, "lockMac"));
		
		LockAttch lockAttch = new LockAttch();
		lockAttch.setFlagPos(MapUtils.getInteger(dataMap, "lockFlagPos"));
		lockAttch.setElectricQuantity(MapUtils.getInteger(dataMap, "electricQuantity"));
		lockAttch.setTimezoneRawOffSet(MapUtils.getLong(dataMap, "timezoneRawOffset"));
		lockAttch.setSpecialValue(MapUtils.getInteger(dataMap, "specialValue"));
		lockAttch.setRemoteEnable(MapUtils.getInteger(dataMap, "remoteEnable"));
		
		LockPwdInfo lockPwdInfo = new LockPwdInfo();
		lockPwdInfo.setKey(MapUtils.getString(dataMap, "lockKey"));
		lockPwdInfo.setAdminPwd(MapUtils.getString(dataMap, "adminPwd"));
		lockPwdInfo.setNoKeyPwd(MapUtils.getString(dataMap, "noKeyPwd"));
		lockPwdInfo.setDeletePwd(MapUtils.getString(dataMap, "deletePwd"));
		lockPwdInfo.setAesKey(MapUtils.getString(dataMap, "aesKeyStr"));
		lockPwdInfo.setKeyboardPwdVersion(MapUtils.getInteger(dataMap, "keyboardPwdVersion"));
		
		boolean exists = lockMapper.existsWithPrimaryKey(lockId);
		if(exists){
			lockMapper.updateByPrimaryKeySelective(lock);
			lockAttchMapper.updateByPrimaryKeySelective(lockAttch);
			lockPwdInfoMapper.updateByPrimaryKeySelective(lockPwdInfo);
		}else{
			lockMapper.insertSelective(lock);
			lockAttchMapper.insertSelective(lockAttch);
			lockPwdInfoMapper.insertSelective(lockPwdInfo);
		}
		
		Map<String, Object> versionMap = (Map<String, Object>) dataMap.get("lockVersion");
		LockVersion lockVersion = new LockVersion();
		lockVersion.setLockId(lockId);
		lockVersion.setProtocolType(MapUtils.getInteger(versionMap, "protocolType"));
		lockVersion.setProtocolType(MapUtils.getInteger(versionMap, "protocolType"));
		lockVersion.setProtocolVersion(MapUtils.getInteger(versionMap, "protocolVersion"));
		lockVersion.setScene(MapUtils.getInteger(versionMap, "scene"));
		lockVersion.setGroupId(MapUtils.getInteger(versionMap, "groupId"));
		lockVersion.setOrgId(MapUtils.getInteger(versionMap, "orgId"));
		lockVersion.setLogoUrl(MapUtils.getString(versionMap, "logoUrl"));
		lockVersion.setShowAdminKbpwdFlag(MapUtils.getString(versionMap, "showAdminKbpwdFlag"));
		boolean lv = lockVersionMapper.existsWithPrimaryKey(lockId);
		if(lv){
			lockVersionMapper.updateByPrimaryKeySelective(lockVersion);
		}else{
			lockVersionMapper.insertSelective(lockVersion);
		}
		
	}

	private void saveOrUpdateKey(Map<String, Object> dataMap, String userId) {
		Key key = new Key();
		key.setUserId(userId);
		Integer keyId = MapUtils.getInteger(dataMap, "keyId");
		key.setKeyId(keyId);
		key.setLockId(MapUtils.getInteger(dataMap, "lockId"));
		key.setUserType(MapUtils.getString(dataMap, "userType"));
		Long startDate = MapUtils.getLong(dataMap, "startDate");
		Long endDate = MapUtils.getLong(dataMap, "endDate");
		key.setStartDate(startDate);
		key.setEndDate(endDate);
		key.setKeyRight(MapUtils.getInteger(dataMap, "keyRight"));
		key.setRemarks(MapUtils.getString(dataMap, "remarks"));
		key.setKeyStatus(MapUtils.getString(dataMap, "keyStatus"));
		boolean k = keyMapper.existsWithPrimaryKey(keyId);
		if(k){
			keyMapper.updateByPrimaryKeySelective(key);
		}else{
			keyMapper.insertSelective(key);
		}
		
	}

	public Key getKeyByUserIdAndLockId(String userId, Integer lockId) {
		return keyMapper.findOne(userId, lockId, null);
	}

	/**
	 * 正常可用的锁数量
	 * @param userId
	 * @return
	 */
	public Integer getNormalKeyCount(String userId) {
		return this.keyMapper.getNormalKeyCount(userId);
	}
	
	public static void main(String[] args) throws ParseException {
		Date parseDate = DateUtils.parseDate("2018-09-28 04:00.000-04:00", "yyyy-MM-dd HH:mm.SSSZZ");
		System.out.println(parseDate);
		/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+" + -4));
		System.out.println(sdf.parse("2018-09-28 04:00"));*/
	}
	
}
