/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.ext.ScienerService;
import com.igloo.smarthome.mapper.IccMapper;
import com.igloo.smarthome.mapper.LockMapper;
import com.igloo.smarthome.mapper.OperationLogMapper;
import com.igloo.smarthome.model.Icc;
import com.igloo.smarthome.model.Lock;

import tcsyn.basic.ext.SystemException;
import tcsyn.basic.util.HttpClientUtil;
import tcsyn.basic.util.JsonUtil;

/**
 * 
 * @author shiwei
 * @date 2018年9月11日
 */
@Service
public class IccService {
	
	@Autowired
	IccMapper iccMapper;
	
	@Value("${sciener.appid}")
	String appid;
	
	@Autowired
	ScienerService scienerService;
	
	@Autowired
	LockMapper lockMapper;
	
	@Autowired
	OperationLogMapper operationLogMapper;
	
	public Icc getById(String cardId) {
		return this.iccMapper.selectByPrimaryKey(cardId);
	}
	
	public Icc getByCardNumber(String cardNumber, Integer lockId) {
		Icc icc = new Icc();
		icc.setCardNumber(cardNumber);
		icc.setLockId(lockId);
		return this.iccMapper.selectOne(icc);
	}
	
	@Async
	public void addIcc(List<Icc> iccList, String userId, Integer lockId) {
		for (Icc icc : iccList) {
			this.addIcc(icc, 1);
		}
	}

	@Transactional
	public void addIcc(Icc icc, Integer addType) {
		
		Integer lockId = icc.getLockId();
		String cardNumber = icc.getCardNumber();
		
		Lock lock = this.lockMapper.selectByPrimaryKey(lockId);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(lock.getUserId()));
		paramMap.put("lockId", lockId.toString());
		paramMap.put("cardNumber", cardNumber);
		Date startDate = icc.getStartDate();
		if (startDate != null) {
			paramMap.put("startDate", String.valueOf(startDate.getTime()));
			paramMap.put("endDate", String.valueOf(icc.getEndDate().getTime()));
		}
		paramMap.put("addType", addType.toString());
		paramMap.put("date", String.valueOf(icc.getCreateDate().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "identityCard/add", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer cardId = (Integer) resultMap.get("cardId");
			if(cardId == null){
				throw new IllegalStateException(result);
			}
			icc.setCardId(cardId);
			this.iccMapper.insertSelective(icc);
			
		} catch (Exception e) {
			throw new SystemException("Failed to add IC card, please try again later", e);
		}
	}
	
	@Transactional
	public void deleteIcc(String cardNumber, Integer deleteType, Integer lockId) {
		Icc icc = this.getByCardNumber(cardNumber, lockId);
		this.iccMapper.deleteByPrimaryKey(icc.getCardId());
		
		//删除ic操作记录
		operationLogMapper.delIccLog(cardNumber);
		
		Lock lock = this.lockMapper.selectByPrimaryKey(lockId);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(lock.getUserId()));
		paramMap.put("lockId", lockId.toString());
		paramMap.put("cardId", icc.getCardId().toString());
		paramMap.put("deleteType", deleteType.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "identityCard/delete", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errcode = (Integer) resultMap.get("errcode");
			if(errcode == null || errcode != 0){
				throw new IllegalStateException(result);
			}
		} catch (Exception e) {
			throw new SystemException("Failed to delete the IC card, please try again later", e);
		}
	}
	
	@Transactional
	public void deleteAllIcc(Integer lockId, boolean delRemoteIcc) {
		Icc icc = new Icc();
		icc.setLockId(lockId);
		this.iccMapper.delete(icc);
		
		if (delRemoteIcc) {
			Lock lock = this.lockMapper.selectByPrimaryKey(lockId);
			Map<String, String> paramMap = new HashMap<>();
			paramMap.put("clientId", this.appid);
			paramMap.put("accessToken", this.scienerService.getAccessToken(lock.getUserId()));
			paramMap.put("lockId", lockId.toString());
			paramMap.put("date", String.valueOf(System.currentTimeMillis()));
			try {
				String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "identityCard/clear", paramMap);
				Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
				Integer errcode = (Integer) resultMap.get("errcode");
				if(errcode == null || errcode != 0){
					throw new IllegalStateException(result);
				}
			} catch (Exception e) {
				throw new SystemException("Failed to Empty the IC card, please try again later", e);
			}
		}
	}
	
	public List<Icc> getIcc(Integer lockId, Integer start, Integer limit, String keyword) {
		return this.iccMapper.getIcc(lockId, start, limit, keyword);
	}

}
