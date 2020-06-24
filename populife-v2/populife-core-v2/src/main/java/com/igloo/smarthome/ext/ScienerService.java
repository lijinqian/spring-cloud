/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.ext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.model.User;
import com.igloo.smarthome.service.UserService;
import com.letsun.cache.annotation.Cache;

import tcsyn.basic.ext.SystemException;
import tcsyn.basic.util.HttpClientUtil;
import tcsyn.basic.util.JsonUtil;
import tcsyn.basic.util.TextUtil;

/**
 * 科技侠服务接口
 * @author win 10
 * @date 2018年8月21日
 */
@Component
public class ScienerService {
	
	@Value("${sciener.appid}")
	String appid;
	
	@Value("${sciener.appsecret}")
	String appsecret;
	
	@Value("${sciener.redirect_uri}")
	String redirectUri;
	
	@Autowired
	UserService userService;
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	/**
	 * 获取访问token
	 * @param userId
	 * @return
	 */
	public String getAccessToken(String userId) {
		ValueOperations<String, String> vo = this.redisTemplate.opsForValue();
		final String accessTokenKey = "access.token." + userId;
		final String refreshTokenKey = "refresh.token." + userId;
		
		String accessToken = vo.get(accessTokenKey);
		if (StringUtils.isNotBlank(accessToken)) {
			return accessToken;
		}
		
		User user = this.userService.getById(userId);
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("client_id", this.appid);
		paramMap.put("client_secret", this.appsecret);
		paramMap.put("grant_type", "password");
		paramMap.put("username", user.getUsername());
		paramMap.put("password", TextUtil.md5(user.getPassword(), 1));
		paramMap.put("redirect_uri", this.redirectUri);
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_TOKEN_PREFIX, paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			accessToken = resultMap.get("access_token").toString();
			if (StringUtils.isBlank(accessToken)) {
				throw new IllegalStateException(result);
			} 
			Integer expiresIn = (Integer) resultMap.get("expires_in");
			String refreshToken = resultMap.get("refresh_token").toString();
			if (expiresIn > 60) {
				vo.set(accessTokenKey, accessToken, expiresIn, TimeUnit.SECONDS);
				vo.set(refreshTokenKey, refreshToken, 365 * 10, TimeUnit.DAYS);
				return accessToken;
			} else {
				refreshToken = vo.get(refreshTokenKey);
				accessToken = this.refreshAccessToken(accessTokenKey, refreshTokenKey);
				return accessToken;
			}
		} catch (Exception e) {
			throw new SystemException("获取token失败，请稍后重试", e);
		}
	}
	
	@Cache(value = "sciener.user.openid", expire = 60 * 60 * 2)
	public Integer getOpenid(String userId) {
		User user = this.userService.getById(userId);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("client_id", this.appid);
		paramMap.put("client_secret", this.appsecret);
		paramMap.put("grant_type", "password");
		paramMap.put("username", user.getUsername());
		paramMap.put("password", TextUtil.md5(user.getPassword(), 1));
		paramMap.put("redirect_uri", this.redirectUri);
		
		String result = null;
		try {
			result = HttpClientUtil.httpPost(Constants.SCIENER_TOKEN_PREFIX, paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer openid = (Integer) resultMap.get("openid");
			if (openid == null) {
				throw new IllegalStateException(result);
			} 
			return openid;
		} catch (Exception e) {
			throw new SystemException("获取openid失败：" + result, e);
		}
	}
	
	/**
	 * 刷新访问Token
	 * @param accessTokenKey
	 * @param refreshTokenKey
	 * @return
	 */
	private String refreshAccessToken(String accessTokenKey, String refreshTokenKey) {
		ValueOperations<String, String> vo = this.redisTemplate.opsForValue();
		String refreshToken = vo.get(refreshTokenKey);
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("client_id", this.appid);
		paramMap.put("client_secret", this.appsecret);
		paramMap.put("grant_type", "refresh_token");
		paramMap.put("refresh_token", refreshToken);
		paramMap.put("redirect_uri", this.redirectUri);
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_TOKEN_PREFIX, paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			String accessToken = resultMap.get("access_token").toString();
			if (StringUtils.isBlank(accessToken)) {
				throw new IllegalStateException(result);
			} 
			Integer expiresIn = (Integer) resultMap.get("expires_in");
			refreshToken = resultMap.get("refresh_token").toString();
			vo.set(accessTokenKey, accessToken, expiresIn, TimeUnit.SECONDS);
			return accessToken;
		} catch (Exception e) {
			throw new SystemException("刷新Token失败，请稍后重试", e);
		}
	}
}
