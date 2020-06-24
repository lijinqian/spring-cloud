package com.igloo.smarthome.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.mapper.LockAttchMapper;
import com.igloo.smarthome.mapper.LockMapper;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.LockAttch;

import tcsyn.basic.ext.SystemException;
import tcsyn.basic.util.HttpClientUtil;
import tcsyn.basic.util.JsonUtil;

@Service
public class LockAttchService extends BaseService{
	
	@Autowired
	LockAttchMapper lockAttchMapper;
	
	@Autowired
	LockMapper lockMapper;
	

	public LockAttch getLockAttchById(Integer lockId) {
		return lockAttchMapper.selectByPrimaryKey(lockId);
	}

	public void updateSpecialValue(LockAttch lockAttch) {
		Lock lock = lockMapper.selectByPrimaryKey(lockAttch.getLockId());
		
		lockAttchMapper.updateByPrimaryKeySelective(lockAttch);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(lock.getUserId()));
		paramMap.put("lockId", String.valueOf(lock.getLockId()));
		paramMap.put("specialValue", lockAttch.getSpecialValue().toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/updateSpecialValue", paramMap);
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}

	@Transactional
	public void updateElectricQuantity(LockAttch lockAttch) {
		Lock lock = lockMapper.selectByPrimaryKey(lockAttch.getLockId());
		
		lockAttchMapper.updateByPrimaryKeySelective(lockAttch);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(lock.getUserId()));
		paramMap.put("lockId", String.valueOf(lockAttch.getLockId()));
		paramMap.put("electricQuantity", String.valueOf(lockAttch.getElectricQuantity()));
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/updateElectricQuantity", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			
		} catch (Exception e) {
			throw new SystemException("上传锁电量失败，请稍后重试", e);
		}
	}
	
}
