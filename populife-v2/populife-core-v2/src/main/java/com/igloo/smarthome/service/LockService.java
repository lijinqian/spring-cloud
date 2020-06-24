/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.ext.ScienerService;
import com.igloo.smarthome.mapper.KeyMapper;
import com.igloo.smarthome.mapper.KeyboardMapper;
import com.igloo.smarthome.mapper.LockAttchMapper;
import com.igloo.smarthome.mapper.LockMapper;
import com.igloo.smarthome.mapper.LockPwdInfoMapper;
import com.igloo.smarthome.mapper.LockVersionMapper;
import com.igloo.smarthome.mapper.UserLockMapper;
import com.igloo.smarthome.model.Key;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.LockAttch;
import com.igloo.smarthome.model.LockPwdInfo;
import com.igloo.smarthome.model.LockVersion;
import com.igloo.smarthome.model.User;
import com.igloo.smarthome.model.UserLock;
import com.igloo.smarthome.model.vo.ExpireKeyVo;
import com.igloo.smarthome.model.vo.LockSetupVo;
import com.igloo.smarthome.model.vo.LockUserVo;
import com.igloo.smarthome.model.vo.LockVo;

import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.ext.SystemException;
import tcsyn.basic.util.HttpClientUtil;
import tcsyn.basic.util.JsonUtil;
import tk.mybatis.mapper.entity.Example;

/**
 * 
 * @author lijq
 * @Date 2018年8月21日
 */
@Service
public class LockService extends BaseService{
	Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	LockMapper lockMapper;
	
	@Autowired
	LockVersionMapper lockVersionMapper;
	
	@Autowired
	KeyMapper keyMapper;
	
	@Value("${key.expire.days}")
	Integer expireDays;
	
	@Autowired
	UserLockMapper userLockMapper;
	
	@Autowired
	KeyboardMapper keyboardMapper;
	
	@Autowired
	UserMessageService userMessageService;
	
	@Autowired
	OperationLogService operationLogService;
	
	@Autowired
	IccService iccService;
	
	@Autowired
	LockAttchMapper lockAttchMapper;
	
	@Autowired
	LockPwdInfoMapper lockPwdInfoMapper;
	
	public List<Lock> getLock4Normal() {
		Lock lock = new Lock();
		lock.setStatus(1);
		return this.lockMapper.select(lock);
	}

	@Transactional
	public Map<String,Object> init(Lock lock, LockAttch lockAttch, LockPwdInfo lockPwdInfo, LockVersion lockVersion) {
		
		Map<String, Integer> versionMap = new HashMap<String, Integer>();
		versionMap.put("protocolType", lockVersion.getProtocolType());
		versionMap.put("protocolVersion", lockVersion.getProtocolVersion());
		versionMap.put("scene", lockVersion.getScene());
		versionMap.put("groupId", lockVersion.getGroupId());
		versionMap.put("orgId", lockVersion.getOrgId());
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(lock.getUserId()));
		paramMap.put("lockName", lock.getName());
		paramMap.put("lockAlias", lock.getAlias());
		paramMap.put("lockMac", lock.getMac());
		paramMap.put("lockKey", lockPwdInfo.getKey());
		paramMap.put("lockFlagPos", String.valueOf(lockAttch.getFlagPos()));
		paramMap.put("adminPwd", lockPwdInfo.getAdminPwd());
		paramMap.put("noKeyPwd", lockPwdInfo.getNoKeyPwd());
		paramMap.put("pwdInfo", lockPwdInfo.getPwdInfo());
		paramMap.put("timestamp", String.valueOf(lockAttch.getTimestamp()));
		paramMap.put("specialValue", String.valueOf(lockAttch.getSpecialValue()));
		paramMap.put("electricQuantity", String.valueOf(lockAttch.getElectricQuantity()));
		paramMap.put("timezoneRawOffset", String.valueOf(lockAttch.getTimezoneRawOffSet()));
		paramMap.put("modelNum", lockAttch.getModelNum());
		paramMap.put("aesKeyStr", lockPwdInfo.getAesKey());
		paramMap.put("hardwareRevision", lockVersion.getHardwareRevision());
		paramMap.put("firmwareRevision", lockVersion.getFirmwareRevision());
		paramMap.put("date", String.valueOf((new Date()).getTime()));
		paramMap.put("lockVersion", JsonUtil.toJson(versionMap));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/init", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer locId = MapUtils.getInteger(resultMap, "lockId");
			Integer keyId = MapUtils.getInteger(resultMap, "keyId");
			if(null == locId || keyId == null){
				throw new IllegalStateException(result);
			}
			
			// 物理键重置锁，删除原有锁数据
			Lock existsLock = this.lockMapper.getUndeletedByMac(lock.getMac());
			if (existsLock != null) {
				String userId = existsLock.getUserId();
				this.deleteLock4Manager(userId, existsLock, existsLock.getKeyId(), false);
				this.userMessageService.addUserMessage(userId, AbstractController.getText("lock.reset.title"), AbstractController.getText("lock.reset.content", existsLock.getAlias()), 11);
			}
			
			this.userMessageService.addInitLockUserMessage(lock.getUserId(), AbstractController.getText("lock.init.title"), AbstractController.getText("lock.init.content", lock.getAlias()), locId);
			
			lock.setLockId(locId);
			lock.setKeyId(keyId);
			lock.setInitDate(new Date());
			lock.setStatus(Constants.LockStatus.NORMAL);
			lockVersion.setLockId(locId);
			lockAttch.setLockId(locId);
			this.addLock(lock, lockVersion, lockAttch, lockPwdInfo);
			
			//删除该锁的所有钥匙
			this.keyMapper.updateKeyStatusByLockId(Constants.KeyStatus.DELETED,  lock.getLockId(), Constants.YES);
			
			//生成一把新钥匙
			Key key = new Key();
			key.setKeyId(lock.getKeyId());
			key.setLockId(lock.getLockId());
			key.setUserId(lock.getUserId());
			key.setUserType(Constants.KeyUserType.MANGER);
			key.setType(Constants.KeyType.FOREVER);
			key.setStartDate(0l);
			key.setEndDate(0l);
			key.setKeyRight(Constants.KeyRight.NO);
			key.setKeyStatus(Constants.KeyStatus.NORMAL);
			this.keyMapper.insertSelective(key);
			this.updateUserLockHome(new UserLock(lock.getUserId(), lock.getLockId()));
			return resultMap;
			
		} catch (Exception e) {
			throw new SystemException("锁初始化失败，请稍后重试", e);
		}
	}
	
	@Transactional
	public Map<String, Object> init_v3(String lockData, String mac, String name, String lockAlias, String userId) {
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(userId));
		paramMap.put("lockData", lockData);
		paramMap.put("lockAlias", lockAlias);
		paramMap.put("date", String.valueOf((new Date()).getTime()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/init", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer locId = MapUtils.getInteger(resultMap, "lockId");
			Integer keyId = MapUtils.getInteger(resultMap, "keyId");
			if(null == locId || keyId == null){
				throw new IllegalStateException(result);
			}
			
			// 物理键重置锁，删除原有锁数据
			Lock existsLock = this.lockMapper.getUndeletedByMac(mac);
			if (existsLock != null) {
				String uId = existsLock.getUserId();
				this.deleteLock4Manager(uId, existsLock, existsLock.getKeyId(), false);
				this.userMessageService.addUserMessage(uId, AbstractController.getText("lock.reset.title"), AbstractController.getText("lock.reset.content", existsLock.getAlias()), 11);
			}
			
			this.userMessageService.addInitLockUserMessage(userId, AbstractController.getText("lock.init.title"), AbstractController.getText("lock.init.content", lockAlias), locId);
			
			Lock lock = new Lock();
			lock.setLockId(locId);
			lock.setKeyId(keyId);
			lock.setMac(mac);
			lock.setName(name);
			lock.setAlias(lockAlias);
			lock.setUserId(userId);
			lock.setInitDate(new Date());
			lock.setStatus(Constants.LockStatus.NORMAL);
			LockAttch lockAttch = new LockAttch();
			lockAttch.setLockId(locId);
			LockPwdInfo lockPwdInfo = new LockPwdInfo();
			lockPwdInfo.setLockId(locId);
			lockPwdInfo.setLockData(lockData);
			LockVersion lockVersion = new LockVersion();
			lockVersion.setLockId(locId);
			this.addLock(lock, lockVersion, lockAttch, lockPwdInfo);
			
			//删除该锁的所有钥匙
			this.keyMapper.updateKeyStatusByLockId(Constants.KeyStatus.DELETED,  lock.getLockId(), Constants.YES);
			
			//生成一把新钥匙
			Key key = new Key();
			key.setKeyId(lock.getKeyId());
			key.setLockId(lock.getLockId());
			key.setUserId(lock.getUserId());
			key.setUserType(Constants.KeyUserType.MANGER);
			key.setType(Constants.KeyType.FOREVER);
			key.setStartDate(0l);
			key.setEndDate(0l);
			key.setKeyRight(Constants.KeyRight.NO);
			key.setKeyStatus(Constants.KeyStatus.NORMAL);
			this.keyMapper.insertSelective(key);
			this.updateUserLockHome(new UserLock(lock.getUserId(), lock.getLockId()));
			return resultMap;
			
		}catch(Exception e){
			throw new SystemException("V3锁初始化失败，请稍后重试", e);
		}
		
	}

	@Transactional
	public void addLock(Lock lock, LockVersion lockVersion, LockAttch lockAttch, LockPwdInfo lockPwdInfo){
		Lock loc = lockMapper.selectByPrimaryKey(lock.getLockId());
		//锁id是惟一的，存在就更新，不存在就插入新记录
		if(null == loc){
			this.lockMapper.insertSelective(lock);
			this.lockVersionMapper.insertSelective(lockVersion);
			this.lockAttchMapper.insertSelective(lockAttch);
			this.lockPwdInfoMapper.insertSelective(lockPwdInfo);
		}else{
			this.lockMapper.updateByPrimaryKeySelective(lock);
			this.lockVersionMapper.updateByPrimaryKeySelective(lockVersion);
			this.lockAttchMapper.updateByPrimaryKeySelective(lockAttch);
			this.lockPwdInfoMapper.updateByPrimaryKeySelective(lockPwdInfo);
		}
	}
	

	public Lock getLockById(Integer lockId) {
		return this.lockMapper.selectByPrimaryKey(lockId);
	}


	@Transactional
	public void deleteAllKey(User user, Lock lock) {
		Key key = new Key();
		key.setKeyStatus(Constants.KeyStatus.DELETED);
		Example example = new Example(Key.class);
		example.createCriteria().andEqualTo("lockId", lock.getLockId())
		.andEqualTo("userType", Constants.KeyUserType.USER).andEqualTo("isClear", Constants.NO)
		.andNotEqualTo("keyStatus", Constants.KeyStatus.RESET);
		keyMapper.updateByExampleSelective(key, example);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("lockId", String.valueOf(lock.getLockId()));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/deleteAllKey", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			
		} catch (Exception e) {
			throw new SystemException("删除所有普通钥匙失败，请稍后重试", e);
		}
		
		
	}

	@Transactional
	public void rename(Lock lock) {
		this.lockMapper.updateByPrimaryKeySelective(lock);
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(lock.getUserId()));
		paramMap.put("lockId", String.valueOf(lock.getLockId()));
		paramMap.put("lockAlias", String.valueOf(lock.getAlias()));
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/rename", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			
			this.userMessageService.updateInitLockUserMessage(lock.getUserId(),  "You become the administrator of the lock[" + lock.getAlias() + "]", lock.getLockId());
			
		} catch (Exception e) {
			throw new SystemException("修改锁名称失败，请稍后重试", e);
		}
		
	}
	
	public List<Lock> getByUserId(String userId) {
		Lock lock = new Lock();
		lock.setUserId(userId);
		lock.setStatus(Constants.LockStatus.NORMAL);
		return this.lockMapper.select(lock);
	}
	
	
	public Lock getLockByKeyId(String userId, Integer keyId) {
		Lock lock  = new Lock();
		lock.setUserId(userId);
		lock.setKeyId(keyId);
		lock.setStatus(Constants.LockStatus.NORMAL);
		return this.lockMapper.selectOne(lock);
	}

	public Map<String, Object> queryDate(Lock lock) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(lock.getUserId()));
		paramMap.put("lockId", String.valueOf(lock.getLockId()));
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/queryDate", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( null ==  MapUtils.getLong(resultMap, "date")){
				throw new IllegalStateException(result);
			}
			return resultMap;
		} catch (Exception e) {
			throw new SystemException("读取锁时间失败，请稍后重试", e);
		}
		
	}

	@Transactional
	public Map<String, Object> updateDate(Lock lock) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(lock.getUserId()));
		paramMap.put("lockId", String.valueOf(lock.getLockId()));
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/updateDate", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( null ==  MapUtils.getLong(resultMap, "date")){
				throw new IllegalStateException(result);
			}
			return resultMap;
		} catch (Exception e) {
			throw new SystemException("校准锁时间失败，请稍后重试", e);
		}
	}

	public List<LockUserVo> getUserList(Integer lockId, String userId, Integer start, Integer pageSize) {
		return lockMapper.getUserList(lockId, userId, start, pageSize);
	}

	public List<ExpireKeyVo> getExpireKeyList(Integer lockId, String userId, Integer start, Integer pageSize) {
		return lockMapper.getExpireKeyList(lockId,  userId,  start,  pageSize, expireDays);
	}
	
	@Transactional
	public void updateUserLockHome(UserLock userLock) {
		UserLock bean = this.userLockMapper.selectByPrimaryKey(userLock);
		if (bean != null) {
			this.userLockMapper.updateByPrimaryKey(userLock);
		} else {
			this.userLockMapper.insertSelective(userLock);
		}
	}

	public LockSetupVo getSetup(String userId, Lock lock) {
		if(StringUtils.equals(userId, lock.getUserId())){
			LockSetupVo managerSetup = lockMapper.getManagerSetup(userId, lock.getLockId());
			managerSetup.setUserType(Constants.KeyUserType.MANGER);
			return managerSetup;
		}
		LockSetupVo normalSetup = lockMapper.getNormalSetup(userId, lock.getLockId());
		normalSetup.setUserType(Constants.KeyUserType.USER);
		return normalSetup;
	}
	
	@Transactional
	public void deleteLock4Manager(String userId, Lock lock, Integer keyId, boolean delRemoteIcc) {
		Integer lockId = lock.getLockId();
		List<String> userIds = keyMapper.getAllUserIdsByLockId(lock.getLockId(), userId, true, true);
		
		lock.setStatus(Constants.LockStatus.DELETED);
		lockMapper.updateByPrimaryKeySelective(lock);
		
		//删除钥匙
		keyMapper.updateKeyStatusByLockId(Constants.KeyStatus.DELETED, lockId, Constants.YES);
		
		//删除密码
		keyboardMapper.delKeyboardByLockId(Constants.KeyboardPwdDeleteType.BLUETOOTH, lockId);
		
		//删除ic卡
		this.iccService.deleteAllIcc(lockId, delRemoteIcc);
		
		//删除操作记录
		operationLogService.deleteByLockId(lockId);
		
		//删除锁分组
		this.userLockMapper.delete(new UserLock(null, lockId));
		
		this.deleteKey(userId, keyId, lock, null, true, userIds);
		
	}
	
	@Transactional
	public void deleteLock4User(String userId, Lock lock) {
		Integer lockId = lock.getLockId();
		List<String> userIds = keyMapper.getAllUserIdsByLockId(lock.getLockId(), userId, false, true);
		
		this.userLockMapper.delete(new UserLock(userId, lockId));
		
		Key ownerKey = this.keyMapper.findNormalOne(userId, lockId);
		if (ownerKey != null) {
			this.keyMapper.updateKeyStatusByLockIdAndSender(Constants.KeyStatus.DELETED, lockId, userId);
			this.deleteKey(userId, ownerKey.getKeyId(), lock, ownerKey, false, userIds);
		}
	}

	/**
	 * 
	 * @param userId
	 * @param keyId
	 */
	@Transactional
	public void deleteKey(String userId, Integer keyId, Lock lock, Key key, Boolean isAdmin, List<String> userIds) {
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
			if(isAdmin){
				userMessageService.batchAddUserMessage(userIds, AbstractController.getText("lock.deleted.title"), AbstractController.getText("lock.deleted.content", lock.getAlias()), 7);
			}else{
				userMessageService.batchAddUserMessage(userIds, AbstractController.getText("key.deleted.title"), AbstractController.getText("key.deleted.content", key.getAlias()), 7);
			}
		} catch (Exception e) {
			throw new SystemException("删除钥匙失败，请稍后重试", e);
		}
	}

	@Transactional
	public void resetKey(User user, Lock lock) {
		//重置所有普通钥匙
		List<String> userIds = keyMapper.getAllUserIdsByLockId(lock.getLockId(), user.getId(), true, false);
		Key key = new Key();
		key.setKeyStatus(Constants.KeyStatus.RESET);
		Example example = new Example(Key.class);
		example.createCriteria().andEqualTo("lockId", lock.getLockId())
		.andEqualTo("userType", Constants.KeyUserType.USER).andEqualTo("isClear", Constants.NO)
		.andNotEqualTo("keyStatus", Constants.KeyStatus.DELETED);
		keyMapper.updateByExampleSelective(key, example);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("lockId", String.valueOf(lock.getLockId()));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/resetKey", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			
			userMessageService.batchAddUserMessage(userIds, AbstractController.getText("key.reset.title"), AbstractController.getText("key.reset.content", lock.getAlias()), 7);
			
		} catch (Exception e) {
			throw new SystemException("重置普通钥匙失败，请稍后重试", e);
		}
		
	}

	@Transactional
	public void cleanKey(User user, Lock lock, Boolean isAdmin) {
		
		List<String> userIds = keyMapper.getAllUserIdsByLockId(lock.getLockId(), user.getId(), isAdmin, false);
		
		if(isAdmin){
			//删除所有普通钥匙
			Key key = new Key();
			key.setKeyStatus(Constants.KeyStatus.DELETED);
			Example example = new Example(Key.class);
			example.createCriteria().andEqualTo("lockId", lock.getLockId())
			.andEqualTo("userType", Constants.KeyUserType.USER);
			keyMapper.updateByExampleSelective(key, example);
		}else{
			//删除授权用户所发送的钥匙
			Key key = new Key();
			key.setKeyStatus(Constants.KeyStatus.DELETED);
			Example example = new Example(Key.class);
			example.createCriteria().andEqualTo("sender_id", user.getId());
			keyMapper.updateByExampleSelective(key, example);
		}
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("lockId", String.valueOf(lock.getLockId()));
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/deleteAllKey", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			
			userMessageService.batchAddUserMessage(userIds, AbstractController.getText("lock.deleted.title"), AbstractController.getText("lock.deleted.content", lock.getAlias()), 7);
			
		} catch (Exception e) {
			throw new SystemException("删除普通钥匙失败，请稍后重试", e);
		}
	}
	
	public Map<String, Object> getLockVersion(Lock lock) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(lock.getUserId()));
		paramMap.put("lockId", String.valueOf(lock.getLockId()));
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/upgradeCheck", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errorcode = (Integer) resultMap.get("errcode");
			if (errorcode != null) {
				throw new IllegalStateException(result);
			}
			return resultMap;
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}
	

	public List<LockVo> list(String userId, Integer start, Integer pageSize) {
		return this.lockMapper.list(userId, start, pageSize);
	}

	public void transfer(User user, User recuser, String lockIdList) {
		
		String[] lockIds = lockIdList.split(",");
		//转移锁
		this.lockMapper.transfer(recuser.getId(), lockIds);
		//转移钥匙
		//转移密码
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("receiverUsername", recuser.getUsername());
		paramMap.put("lockIdList", "[" + lockIdList + "]");
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/transfer", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errorcode = (Integer) resultMap.get("errcode");
			if (errorcode != 0) {
				throw new IllegalStateException(result);
			}
		} catch (Exception e) {
			throw new SystemException(e);
		}
		
	}

	public void updateLock(Lock lock) {
		this.lockMapper.updateByPrimaryKeySelective(lock);
		
	}

}
