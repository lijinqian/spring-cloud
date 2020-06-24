package com.igloo.smarthome.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.mapper.LockMapper;
import com.igloo.smarthome.mapper.LockPwdInfoMapper;
import com.igloo.smarthome.model.LockPwdInfo;
import com.igloo.smarthome.model.User;

import tcsyn.basic.ext.SystemException;
import tcsyn.basic.util.HttpClientUtil;
import tcsyn.basic.util.JsonUtil;

@Service
public class LockPwdInfoService extends BaseService{
	
	@Autowired
	LockPwdInfoMapper lockPwdInfoMapper;
	
	@Autowired
	LockMapper lockMapper;
	
	public LockPwdInfo getLockPwdInfoById(Integer lockId) {
		return lockPwdInfoMapper.selectByPrimaryKey(lockId);
	}
	
	
	@Transactional
	public void changeDeletePwd(User user, LockPwdInfo lockPwdInfo) {
		lockPwdInfoMapper.updateByPrimaryKeySelective(lockPwdInfo);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(user.getId()));
		paramMap.put("lockId", String.valueOf(lockPwdInfo.getLockId()));
		paramMap.put("password", lockPwdInfo.getDeletePwd());
		paramMap.put("date", String.valueOf(new Date().getTime()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/changeDeletePwd", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			if( 0 != MapUtils.getInteger(resultMap, "errcode")){
				throw new IllegalStateException(result);
			}
			
		} catch (Exception e) {
			throw new SystemException("修改锁的清空密码失败，请稍后重试", e);
		}
		
	}
	
	@Transactional
	public void modifyAdminPassword(Integer lockId, String password, String userId) {
		LockPwdInfo lockPwdInfo = new LockPwdInfo();
		lockPwdInfo.setLockId(lockId);
		lockPwdInfo.setNoKeyPwd(password);
		this.lockPwdInfoMapper.updateByPrimaryKeySelective(lockPwdInfo);
		
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", scienerService.getAccessToken(userId));
		paramMap.put("lockId", String.valueOf(lockId));
		paramMap.put("password", password);
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/changeAdminKeyboardPwd", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errorCode = (Integer) resultMap.get("errcode");
			if (errorCode == null || errorCode != 0){
				throw new IllegalStateException(result);
			}
		} catch (Exception e) {
			throw new SystemException("修改锁的管理员键盘密码失败，请稍后重试", e);
		}
	}
	
}
