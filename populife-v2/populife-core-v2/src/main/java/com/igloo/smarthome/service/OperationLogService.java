/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.ext.ScienerService;
import com.igloo.smarthome.mapper.KeyMapper;
import com.igloo.smarthome.mapper.KeyboardMapper;
import com.igloo.smarthome.mapper.OperationLogMapper;
import com.igloo.smarthome.mapper.UserLockMapper;
import com.igloo.smarthome.model.Key;
import com.igloo.smarthome.model.Keyboard;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.OperationLog;
import com.igloo.smarthome.model.vo.OperationLogVo;

import tcsyn.basic.util.JsonUtil;
import tcsyn.basic.util.TextUtil;
import tk.mybatis.mapper.entity.Example;

/**
 * 
 * @author shiwei
 * @date 2018年9月1日
 */
@Service
public class OperationLogService {
	
	@Autowired
	OperationLogMapper operationLogMapper;
	
	@Autowired
	UserLockMapper userLockMapper;
	
	@Autowired
	KeyMapper keyMapper;
	
	@Autowired
	LockService lockService;
	
	@Autowired
	ScienerService scienerService;
	
	@Autowired
	KeyboardService KeyboardService;
	
	@Autowired
	KeyboardMapper keyboardMapper;
	
	@Autowired
	UserService userService;
	
	@Value("${sciener.appid}")
	String appid;
	
	@Autowired
	IccService iccService;
	
	Logger logger = Logger.getLogger(getClass());
	
	public void updateOperationLog(OperationLog operationLog) {
		this.operationLogMapper.updateByPrimaryKeySelective(operationLog);
	}
	
	public void removeAll(Integer lockId, String userId) {
		int userType = 0;
		Lock lock = this.lockService.getLockById(lockId);
		if (StringUtils.equals(lock.getUserId(), userId)) {
			userType = 1;
		} else {
			Key key = this.keyMapper.selectOne(new Key(userId, Constants.KeyStatus.NORMAL, lockId));
			if (key != null && StringUtils.equals(key.getUserType(), Constants.KeyUserType.MANGER)) {
				userType = 2;
			} else {
				userType = 3;
			}
		}
		this.operationLogMapper.removeAll(lockId, userId, userType);
	}
	
	public void addKeyLog(String userId, Integer lockId, String content, Integer event, Integer keyId) {
		OperationLog log = new OperationLog();
		log.setId(TextUtil.generateId());
		log.setCreateDate(new Date());
		log.setEvent(event);
		log.setContent(content);
		log.setUserId(userId);
		log.setKeyId(keyId);
		log.setLockId(lockId);
		this.operationLogMapper.insertSelective(log);
		
		if(2 == event){
			//对于单次的钥匙,置为删除
			Example example  = new Example(Key.class);
			example.createCriteria().andEqualTo("userId", userId)
			.andEqualTo("lockId", lockId)
			.andEqualTo("type", Constants.KeyType.ONE)
			.andEqualTo("keyStatus", Constants.KeyStatus.NORMAL);
			List<Key> keys = keyMapper.selectByExample(example);
			for(Key key : keys){
				key.setKeyStatus(Constants.KeyStatus.DELETED);
				keyMapper.updateByPrimaryKeySelective(key);
			}
		}
		
	}
	
	public void addLog(OperationLog operationLog) {
		this.operationLogMapper.insertSelective(operationLog);
	}
	
	public void addLog(String userId, Integer lockId, String content, Integer event, Integer keyId) {
		OperationLog log = new OperationLog();
		log.setId(TextUtil.generateId());
		log.setCreateDate(new Date());
		log.setEvent(event);
		log.setContent(content);
		log.setUserId(userId);
		log.setKeyId(keyId);
		log.setLockId(lockId);
		this.operationLogMapper.insertSelective(log);
	}
	
	public List<OperationLogVo> getLog(Integer lockId, String keyword, Integer start, Integer limit, String userId) {
		int userType = 0;
		Lock lock = this.lockService.getLockById(lockId);
		if (StringUtils.equals(lock.getUserId(), userId)) {
			//管理员用户
			userType = 1;
		} else {
			Key key = this.keyMapper.selectOne(new Key(userId, Constants.KeyStatus.NORMAL, lockId));
			if (key != null && StringUtils.equals(key.getUserType(), Constants.KeyUserType.USER) && key.getKeyRight() ==  1) {
//				//授权用户
				userType = 2;
			} else {
				//普通用户
				userType = 3;
			}
		}
		return this.operationLogMapper.getLog(lockId, keyword, start, limit, userId, userType);
	}
	
	public List<OperationLogVo> getLog(Integer lockId) {
		return operationLogMapper.getLogByLockId(lockId);
	}
	
	public List<OperationLogVo> getLog(Integer keyId, Integer start, Integer limit) {
		return this.operationLogMapper.getLog4Key(keyId, start, limit);
	}
	
	public List<OperationLog> getLog(String password, Integer start, Integer limit) {
		return this.operationLogMapper.getLog4Password(password, start, limit);
	}

	public void addKeyboardLog(String userId, Integer lockId, String records) throws Exception {
		String keyboarContent = "Unlock with keypad"; //hafele项目
//		String keyboarContent = "Unlock with passcode";//populife项目
		String iccContent = "Unlock with icc";
		records = records.replaceAll("\\\\", "");
		List<Map<String, Object>> recordList = JsonUtil.fromJson(records, new TypeReference<List<Map<String, Object>>>() {});
		Lock lock = this.lockService.getLockById(lockId);
		List<OperationLog> logList  = new ArrayList<OperationLog>();
		for(Map<String, Object> recordMap : recordList){
			Integer recordType = (Integer) recordMap.get("recordType");
			if (recordType != null) {
				String password = MapUtils.getString(recordMap, "password");
				Long operateDate = MapUtils.getLong(recordMap, "operateDate");
				if(4 == recordType || 8 == recordType){
					this.savePasswordUnlockLog(userId, lockId, keyboarContent, logList, recordMap, password, operateDate);
					if (recordType == 8) {
						
						//当有使用过的密码时，输入清空码会删除密码及清空码；当没有使用过的密码时，不删除清空码
						Integer count = KeyboardService.getUsedKeyboards(lock.getLockId(), password);
						if(count > 0){
							KeyboardService.deleteUsedKeyboard(lock.getLockId(), Constants.KeyboardPwdDeleteType.BLUETOOTH);
						}
						
					} 
				}else if(17 == recordType){
					OperationLog log = new OperationLog();
					log.setId(TextUtil.generateId());
					log.setCreateDate(new Date(operateDate));
					log.setContent(iccContent);
					log.setUserId(userId);
					log.setLockId(lockId);
					log.setPassword(password);
					//icc卡
					log.setEvent(4);
					log.setType(3);
					logList.add(log);
				}
			}
		}
		if(CollectionUtils.isNotEmpty(logList)){
			this.operationLogMapper.batchInsert(logList);
		}
	}

	/**
	 * 
	 * @param userId
	 * @param lockId
	 * @param keyboarContent
	 * @param logList
	 * @param recordMap
	 * @param password
	 * @param operateDate
	 */
	public void savePasswordUnlockLog(String userId, Integer lockId, String keyboarContent, List<OperationLog> logList,
			Map<String, Object> recordMap, String password, Long operateDate) {
		String newPassword = MapUtils.getString(recordMap, "newPassword");
		OperationLog log = new OperationLog();
		log.setId(TextUtil.generateId());
		log.setCreateDate(new Date(operateDate));
		log.setContent(keyboarContent);
		log.setUserId(userId);
		log.setLockId(lockId);
		log.setPassword(password);
		log.setNewPassword(newPassword);
		//密码开锁
		log.setEvent(3);
		log.setType(2);
		
		//密码状态
		Keyboard keyboard = new Keyboard();
		keyboard.setKeyboardPwd(password);
		List<Keyboard> list = keyboardMapper.select(keyboard);
		if (CollectionUtils.isNotEmpty(list)) {
			log.setKeyId(list.get(0).getKeyId());
		}
		logList.add(log);
		
		for (Keyboard keywordBean : list) {
			if(null == keywordBean.getFirstTime() || keywordBean.getFirstTime().getTime() > operateDate){
				keywordBean.setFirstTime(new Date(operateDate));
				keyboardMapper.updateByPrimaryKeySelective(keywordBean);
			}
		}
	}

	public void deleteByLockId(Integer lockId) {
		OperationLog operationLog = new OperationLog();
		operationLog.setIsDelete(Constants.YES);
		
		Example example = new Example(OperationLog.class);
		example.createCriteria().andEqualTo("lockId", lockId).andEqualTo("isDelete", Constants.NO);
		operationLogMapper.updateByExampleSelective(operationLog, example);
	}

	public void deleteByKeyId(Integer keyId) {
		OperationLog operationLog = new OperationLog();
		operationLog.setIsDelete(Constants.YES);
		
		Example example = new Example(OperationLog.class);
		example.createCriteria().andEqualTo("keyId", keyId).andEqualTo("isDelete", Constants.NO);
		operationLogMapper.updateByExampleSelective(operationLog, example);
		
	}
	

}
