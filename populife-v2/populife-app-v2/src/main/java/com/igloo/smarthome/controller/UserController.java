/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.constant.UserLogAnno;
import com.igloo.smarthome.ext.AliyunService;
import com.igloo.smarthome.ext.EmailService;
import com.igloo.smarthome.ext.PushService;
import com.igloo.smarthome.ext.ScienerService;
import com.igloo.smarthome.model.User;
import com.igloo.smarthome.service.UserMessageService;
import com.igloo.smarthome.service.UserService;
import com.igloo.smarthome.service.basic.UserLogService;

import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.model.ExceptionCode;
import tcsyn.basic.model.ResponseModel;
import tcsyn.basic.util.JsonUtil;
import tcsyn.basic.util.TextUtil;

/**
 * 用户接口
 * @author shiwei
 * @date 2018年8月19日
 */
@Controller
@RequestMapping("user")
public class UserController extends AbstractAppController {
	static Logger logger = LoggerFactory.getLogger(UserController.class);
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	AliyunService aliyunService;
	
	@Autowired
	EmailService emailService;     
	
	@Value("${verify.email.subject}")
	String emailSubject;
	
	@Value("${verify.email.content}")
	String emailContent;
	
	@Value("${app.name}")
	String appName;
	
	@Value("${current.env}")
	String env;
	
	@Autowired
	UserService userService;
	
	@Autowired
	ScienerService scienerService; 
	
	@Autowired
	UserMessageService userMessageService;
	
	@Autowired
	UserLogService userLogService;
	
	static final String VERIFY_CODE_KEY_PREFIX = "verify.code.";
	
	static final String LOGIN_VERIFY_CODE_KEY_PREFIX = "login.verify.code.";
	
	static final String RETRIEVE_VERIFY_CODE_KEY_PREFIX = "retrieve.password.verify.code.";
	
	@Autowired
	PushService pushService;
	
	final int limit = 5;
	
	/**
	 * @apiDefine UserGroup 用户
	 */
	
	/**
	 * @apiDefine SuccessParam
	 * @apiSuccess {boolean} success 本次请求处理结果，true：成功，false：失败
	 * @apiSuccess {Integer} code 请求结果状态码
	 * @apiSuccess {String} msg 提示信息
	 * 
	 */
	
	/**
	 * @apiDefine ErrorExample 异常示例
	 * @apiErrorExample {json} 系统错误
	 * {
     *		"success": false,
     *		"code": 500,
     *		"msg": "The server encountered an unexpected, error code: xxxxxx",
     *		"data": null
	 * }
	 * 
	 */
	
	/**
	 * @api {POST} user/send/code 注册--发送验证码
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 用户注册、绑定手机号、绑定邮箱地址时发送的验证码
	 * 
	 * @apiParam {String} username 手机号（带国家区号）或邮件地址
	 * @apiParam {String} [country] 国家编码(如：+86)
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 手机号被使用
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "The phone number has been used",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 邮箱已经被使用
	 * {
		    "success": false,
		    "code": 952,
		    "msg": "The email has been registered",
		    "data": null
	   }
	   
	   @apiErrorExample {json} type参数值错误
	 * {
		    "success": false,
		    "code": 910,
		    "msg": "The value of type is undefinded",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("send/code")
	@ResponseBody
	public ResponseModel sendCode(String username,  String country) {
		logger.info("username:{},type:{},country:{}",username,country);
		if (StringUtils.isBlank(username)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		String code = TextUtil.generateVerifyCode();
		Integer type = 1;
		if(username.contains("@")) {
			type = 2;
		}
		if (type == 1) {
			// 发送短信
			User user = this.userService.getByPhone(username);
			if (user != null) {
				logger.info("The phone number has been used");
				return super.fail("The phone number has been used", ExceptionCode.BX1);
			}
			logger.info("{} haven't registed",username);
			SendSmsResponse sendSmsResponse = this.aliyunService.sendSms(username, code, country);
			if (sendSmsResponse != null) {
				logger.info("sendSmsResponse code:{}",sendSmsResponse.getCode());
				if (!sendSmsResponse.getCode().equals("OK")) {
					if (StringUtils.equals(sendSmsResponse.getCode(), "isv.BUSINESS_LIMIT_CONTROL")) {
						return super.fail("Sent sms exceeded limit", ExceptionCode.BX5);
					}
					return super.fail("Sent sms failure", ExceptionCode.BX4);
				}
			}
			logger.info("{} success!",username);
		} else if (type == 2) {
			// 发送邮件
			User user = this.userService.getByEmail(username);
			if (user != null) {
				logger.info("he email has been registered");
				return super.fail("The email has been registered", ExceptionCode.BX2);
			}
			logger.info("{} haven't registed",username);
			String content = String.format(this.emailContent, code, this.limit);
			String subject = String.format(this.emailSubject, this.appName);
			logger.info("send email");
			this.emailService.sendEmail(username, subject, content, code);
		} else {
			return super.fail("The value of type is undefinded", ExceptionCode.ARG_INVALID);
		}
		logger.info("send success");
		this.redisTemplate.opsForValue().set(VERIFY_CODE_KEY_PREFIX + username, code, this.limit, TimeUnit.MINUTES);
		return super.success();
	}
	

	/**
	 * @api {POST} user/register 用户注册
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 验证用户手机号或者邮件地址后，根据输入的密码、验证码提交注册，注册成功后返回用户id值
	 * 
	 * @apiParam {String} username 手机号（带国家区号）或邮件地址
	 * @apiParam {String} password 用户密码
	 * @apiParam {String} code 验证码
	 * @apiParam {String} [deviceId] 设备id
	 * @apiParam {String} [apnsToken] 发送消息Token
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {String} data 用户id
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": "yvrbe8js41"
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 手机号已经注册
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "The phone number has been registered",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 邮箱已经注册
	 * {
		    "success": false,
		    "code": 952,
		    "msg": "The email has been registered",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 验证码失效
	 * {
		    "success": false,
		    "code": 953,
		    "msg": "Verification code has expired",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 验证码错误
	 * {
		    "success": false,
		    "code": 953,
		    "msg": "Verification code was wrong",
		    "data": null
	   }
	   
	   @apiErrorExample {json} type参数值错误
	 * {
		    "success": false,
		    "code": 910,
		    "msg": "The value of type is undefinded",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("register")
	@ResponseBody
	public ResponseModel register(String code, String username, String password,  String deviceId, String apnsToken, HttpServletRequest request) {
		String optSystem = this.getOptSystem();
		if (!StringUtils.isNoneBlank(code, username, password)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		String cacheCode = this.redisTemplate.opsForValue().get(VERIFY_CODE_KEY_PREFIX + username);
		if (StringUtils.isBlank(cacheCode)) {
			return super.fail("Verification code has expired", ExceptionCode.BX3);
		}
		if (!StringUtils.equals(cacheCode, code)) {
			return super.fail("Verification code was wrong", ExceptionCode.BX4);
		}
		
		String phone = null;
		String email = null;
		Integer type = 1;
		if(username.contains("@")) {
			type = 2;
		}
		if (type == 1) {
			// 发送短信
			User user = this.userService.getByPhone(username);
			if (user != null) {
				return super.fail("The phone number has been registered", ExceptionCode.BX1);
			}
			phone = username;
		} else if (type == 2) {
			// 发送邮件
			User user = this.userService.getByEmail(username);
			if (user != null) {
				return super.fail("The email has been registered", ExceptionCode.BX2);
			}
			email = username;
		} else {
			return super.fail("The value of type is undefinded", ExceptionCode.ARG_INVALID);
		}
		User user = new User();
		user.setId(TextUtil.generateId());
		user.setDeviceId(deviceId);
		user.setEmail(email);
		user.setPhone(phone);
		user.setPassword(password);
		user.setOptSystem(optSystem);
		user.setRegisteredDate(new Date());
		user.setAccountType(type);
		user.setIsDeleted(Constants.NO);
		user.setApnsToken(apnsToken);
		this.userService.addUser(user);
		this.userLogService.addUserLog(new Date(), JsonUtil.toJson(request.getParameterMap()), user.getId(), true, "用户注册", JsonUtil.toJson(super.success()));
		this.redisTemplate.delete(VERIFY_CODE_KEY_PREFIX + username);
		return super.successData(user.getId());
	}
	
	/**
	 * @api {POST} user/login/bycode/send/code 验证码登陆--发送验证码
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 验证码登陆前，发送验证雄码
	 * 
	 * @apiParam {String} username 手机号（带国家区号）或邮件地址
	 * @apiParam {String} [country] 国家编码(如：+86)
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {                                   
	 *     "success": true,               
	 *     "code": 200,                   
	 *     "msg": null,                   
	 *     "data": null                   
	 * }                                  
	 *                                     
	 * @apiErrorExample {json} 必填参数为空      
	 * {                                   
	 *     "success": false,              
	 *     "code": 900,                   
	 *     "msg": "Some parameters are required",             
	 *     "data": null                   
	 * }                                   
	 *                                     
	 * @apiErrorExample {json} 手机或邮箱未注册     
	 * {                                   
	 *     "success": false,              
	 *     "code": 920,                   
	 *     "msg": "The phone number is not registered or bound",                
	 *     "data": null                   
	 * }                                   
	 *                                     
	 * @apiUse ErrorExample                
	 *	
	 */
	@RequestMapping("login/bycode/send/code")
	@ResponseBody
	public ResponseModel loginBycodeSendCode(String username, String country) {
		if ( StringUtils.isBlank(username)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		User user = null;
		String code = TextUtil.generateVerifyCode();
		if (username.contains("@")) {
			user = this.userService.getByEmail(username);
			if(null == user){
				return super.fail("The email is not registered or bound", ExceptionCode.ENTITY_EMPRY);
			}
			String content = String.format(this.emailContent, code, this.limit);
			String subject = String.format(this.emailSubject, this.appName);
			this.emailService.sendEmail(username, subject, content, code);
		}else{
			user = this.userService.getByPhone(username);
			if(null == user){
				return super.fail("The phone number is not registered or bound", ExceptionCode.ENTITY_EMPRY);
			}
			SendSmsResponse sendSmsResponse = this.aliyunService.sendSms(username, code, country);
			if (sendSmsResponse != null) {
				if (!sendSmsResponse.getCode().equals("OK")) {
					if (StringUtils.equals(sendSmsResponse.getCode(), "isv.BUSINESS_LIMIT_CONTROL")) {
						return super.fail("Sent sms exceeded limit", ExceptionCode.BX5);
					}
					System.out.println("code: " + sendSmsResponse.getCode() + "msg: " + sendSmsResponse.getMessage());
					return super.fail("Sent sms failure", ExceptionCode.BX4);
				}
			}
		}
		this.redisTemplate.opsForValue().set(LOGIN_VERIFY_CODE_KEY_PREFIX + username, code, this.limit, TimeUnit.MINUTES);
		return super.success();
	}
	
	
	/**
	 * @api {POST} user/login/bycode 验证码登录
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 输入用户名（邮箱地址、手机号）、密码登录，登录后返回用户id，前端缓存用户id作为后续访问的用户标识
	 * 
	 * @apiParam {String} username 手机号（带国家区号）或邮件地址
	 * @apiParam {String} code 验证码
	 * @apiParam {String} [deviceId] 设备id
	 * @apiParam {String} [apnsToken] 发送消息Token
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {User} data 用户信息
	 * 
	 * @apiSuccess (User) {String} userId 用户id
	 * @apiSuccess (User) {String} lockCount 当前用户的正常可用的锁数量
	 * @apiSuccess (User) {String} phone 用户手机号 （异地登录并且绑定了手机号才会返回该参数）
	 * @apiSuccess (User) {String} email 邮箱地址 （异地登录使用邮箱注册未绑定手机号时返回，与“用户手机号”二者返回其一）
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": {
		    	"userId": "yvrbe8js41",
		    	"phone": "18566720111",
		    	"lockCount": "3"
		    }
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 用户名或密码错误
	 * {
		    "success": false,
		    "code": 910,
		    "msg": "Username or password was wrong",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("login/bycode")
	@ResponseBody
	public ResponseModel loginBycode(String username, String code, String deviceId, String apnsToken, HttpServletRequest request) {
		logger.info("login username:{},code:{},deviceId:{},apnsToken:{}",username,code,deviceId,apnsToken);
		String optSystem = this.getOptSystem();
		if (!StringUtils.isNoneBlank(username, code)) {
			logger.info("!StringUtils.isNoneBlank(username{}, code{}, deviceId{})",username,code,deviceId,apnsToken);
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		String cacheCode = this.redisTemplate.opsForValue().get(LOGIN_VERIFY_CODE_KEY_PREFIX + username);
		if (StringUtils.isBlank(cacheCode)) {
			return super.fail("Verification code has expired", ExceptionCode.BX3);
		}
		if (!StringUtils.equals(cacheCode, code)) {
			return super.fail("Verification code was wrong", ExceptionCode.BX4);
		}
		
		User user = null;
		if (username.contains("@")) {
			user = this.userService.getByEmail(username);
		} else {
			user = this.userService.getByPhone(username);
		}
		if (user == null || StringUtils.equals(user.getIsDeleted(), Constants.YES)) {
			logger.info("user is null or  已经删除");
			return super.fail("User is empty!", ExceptionCode.ARG_INVALID);
		}
		String userId = user.getId();
		this.userLogService.addUserLog(new Date(), JsonUtil.toJson(request.getParameterMap()), userId, true, "验证码登录", JsonUtil.toJson(super.success()));
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("userId", userId);
		// 登录同一设备推送通知消息
		if (StringUtils.isNotBlank(user.getDeviceId()) && !StringUtils.equals(user.getDeviceId(), deviceId)) {
			logger.info("deviceId:{},推送",user.getDeviceId());
			String phone = user.getPhone();
			if (StringUtils.isNotBlank(phone)) {
				resultMap.put("phone", user.getPhone());
			} else {
				resultMap.put("email", user.getEmail());
			}
		} else {
			// 更新登录的设备id
			logger.info("更新登陆的设备id:{}",user.getDeviceId());
			User modifyUser = new User();
			modifyUser.setId(userId);
			modifyUser.setOptSystem(optSystem);
			modifyUser.setDeviceId(deviceId);
			apnsToken = apnsToken == null ? "" : apnsToken; 
			modifyUser.setApnsToken(apnsToken);
			this.userService.updateUserAndDevice(modifyUser);
		}
		
		//正常可用的锁数量
//		resultMap.put("lockCount", String.valueOf(this.keyService.getNormalKeyCount(user.getId())));
		logger.info("resultMap:{}",resultMap);
		this.redisTemplate.delete(LOGIN_VERIFY_CODE_KEY_PREFIX + username);
		return super.successData(resultMap);
	}
	
	/**
	 * @api {POST} user/login/bypass 密码登录
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 输入用户名（邮箱地址、手机号）、通过接收到的验证码登陆，登录后返回用户id，前端缓存用户id作为后续访问的用户标识
	 * 
	 * @apiParam {String} username 手机号（带国家区号）或邮件地址
	 * @apiParam {String} password 密码
	 * @apiParam {String} [deviceId] 设备id
	 * @apiParam {String} [apnsToken] 发送消息Token
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {User} data 用户信息
	 * 
	 * @apiSuccess (User) {String} userId 用户id
	 * @apiSuccess (User) {String} lockCount 当前用户的正常可用的锁数量
	 * @apiSuccess (User) {String} phone 用户手机号 （异地登录并且绑定了手机号才会返回该参数）
	 * @apiSuccess (User) {String} email 邮箱地址 （异地登录使用邮箱注册未绑定手机号时返回，与“用户手机号”二者返回其一）
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": {
		    	"userId": "yvrbe8js41",
		    	"phone": "18566720111",
		    	"lockCount": "3"
		    }
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 用户名或密码错误
	 * {
		    "success": false,
		    "code": 910,
		    "msg": "Username or password was wrong",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("login/bypass")
	@ResponseBody
	public ResponseModel loginByPass(String username, String password, String deviceId, String apnsToken, HttpServletRequest request) {
		logger.info("login username:{},password:{},deviceId:{},apnsToken:{}",username,password,deviceId,apnsToken);
		String optSystem = this.getOptSystem();
		if (!StringUtils.isNoneBlank(username, password)) {
			logger.info("!StringUtils.isNoneBlank(username{}, password{}, deviceId{})",username,password,deviceId,apnsToken);
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = null;
		if (username.contains("@")) {
			user = this.userService.getByEmail(username);
		} else {
			user = this.userService.getByPhone(username);
		}
		if (user == null || !StringUtils.equals(user.getPassword(), TextUtil.md5(password, 3))
				|| StringUtils.equals(user.getIsDeleted(), Constants.YES)) {
			logger.info("user is null or 密码不对 或者 已经删除");
			return super.fail("Username or password was wrong", ExceptionCode.ARG_INVALID);
		}
		String userId = user.getId();
		this.userLogService.addUserLog(new Date(), JsonUtil.toJson(request.getParameterMap()), userId, true, "密码登录", JsonUtil.toJson(super.success()));
		
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("userId", userId);
		// 登录同一设备推送通知消息
		if (StringUtils.isNotBlank(user.getDeviceId()) && !StringUtils.equals(user.getDeviceId(), deviceId)) {
			logger.info("deviceId:{},推送",user.getDeviceId());
			String phone = user.getPhone();
			if (StringUtils.isNotBlank(phone)) {
				resultMap.put("phone", user.getPhone());
			} else {
				resultMap.put("email", user.getEmail());
			}
		} else {
			// 更新登录的设备id
			logger.info("更新登陆的设备id:{}",user.getDeviceId());
			User modifyUser = new User();
			modifyUser.setId(userId);
			modifyUser.setOptSystem(optSystem);
			modifyUser.setDeviceId(deviceId);
			apnsToken = apnsToken == null ? "" : apnsToken; 
			modifyUser.setApnsToken(apnsToken);
			this.userService.updateUserAndDevice(modifyUser);
		}
		
		//正常可用的锁数量
//		resultMap.put("lockCount", String.valueOf(this.keyService.getNormalKeyCount(user.getId())));
		logger.info("resultMap:{}",resultMap);
		return super.successData(resultMap);
	}

	private String getOptSystem() {
		String userAgent = super.getRequest().getHeader("user-agent");
		String optSystem = null;
		if (userAgent.contains("iPhone") || userAgent.contains("iPad")  || userAgent.contains("iPod")  || userAgent.contains("iOS")) {
			optSystem = Constants.System.IOS;
		} else if (userAgent.contains("Android") || userAgent.contains("Adr")) {
			optSystem = Constants.System.ANDROID;
		} else {
			optSystem = Constants.System.PC;
		}
		return optSystem;
	}
	
	/**
	 * @api {POST} user/retrieve/password/send/code 重置密码--发送验证码
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 重置密码前，会验证当前账号是否是本人，向手机号或邮箱发送6位数字的验证码，用户收到验证码后，在app中输入正确的6位验证码进入重置密码
	 * 
	 * @apiParam {String} username 手机号（带国家区号）或邮件地址
	 * @apiParam {String} [country] 国家编码(如：+86)
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {                                   
	 *     "success": true,               
	 *     "code": 200,                   
	 *     "msg": null,                   
	 *     "data": null                   
	 * }                                  
	 *                                     
	 * @apiErrorExample {json} 必填参数为空      
	 * {                                   
	 *     "success": false,              
	 *     "code": 900,                   
	 *     "msg": "Some parameters are required",             
	 *     "data": null                   
	 * }                                   
	 *                                     
	 * @apiErrorExample {json} 手机或邮箱未注册     
	 * {                                   
	 *     "success": false,              
	 *     "code": 920,                   
	 *     "msg": "The phone number is not registered or bound",                
	 *     "data": null                   
	 * }                                   
	 *                                     
	 * @apiUse ErrorExample                
	 *	
	 */
	@RequestMapping("retrieve/password/send/code")
	@ResponseBody
	public ResponseModel retrievePasswordSendCode(String username, String country) {
		if ( StringUtils.isBlank(username)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		User user = null;
		String code = TextUtil.generateVerifyCode();
		if (username.contains("@")) {
			user = this.userService.getByEmail(username);
			if(null == user){
				return super.fail("The email is not registered or bound", ExceptionCode.ENTITY_EMPRY);
			}
			String content = String.format(this.emailContent, code, this.limit);
			String subject = String.format(this.emailSubject, this.appName);
			this.emailService.sendEmail(username, subject, content, code);
		}else{
			user = this.userService.getByPhone(username);
			if(null == user){
				return super.fail("The phone number is not registered or bound", ExceptionCode.ENTITY_EMPRY);
			}
			SendSmsResponse sendSmsResponse = this.aliyunService.sendSms(username, code, country);
			if (sendSmsResponse != null) {
				if (!sendSmsResponse.getCode().equals("OK")) {
					if (StringUtils.equals(sendSmsResponse.getCode(), "isv.BUSINESS_LIMIT_CONTROL")) {
						return super.fail("Sent sms exceeded limit", ExceptionCode.BX5);
					}
					System.out.println("code: " + sendSmsResponse.getCode() + "msg: " + sendSmsResponse.getMessage());
					return super.fail("Sent sms failure", ExceptionCode.BX4);
				}
			}
		}
		this.redisTemplate.opsForValue().set(RETRIEVE_VERIFY_CODE_KEY_PREFIX + username, code, this.limit, TimeUnit.MINUTES);
		return super.success();
	}
	
	
	
	/**
	 * @api {POST} user/retrieve/password 提交重置密码
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 提交找回密码请求，输入验证码及新密码，服务器验证码通过后，更新用户的新密码
	 * 
	 * @apiParam {String} username 手机号（带国家区号）或邮件地址
	 * @apiParam {String} password 用户密码
	 * @apiParam {String} code 验证码
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {String} data 用户id
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:        
	 * {                                          
	 *     "success": true,                      
	 *     "code": 200,                          
	 *     "msg": null,                          
	 *     "data": null                          
	 * }                                         
	 *                                            
	 * @apiErrorExample {json} 必填参数为空             
	 * {                                          
	 *     "success": false,                     
	 *     "code": 900,                          
	 *     "msg": "Some parameters are required",                    
	 *     "data": null                          
	 * }                                          
	 *                                            
	 * @apiErrorExample {json} 验证码失效      
	 * {                                          
	 *     "success": false,                     
	 *     "code": 953,                          
	 *     "msg": "Verification code has expired",                      
	 *     "data": null                          
	 * }                                          
	 * @apiErrorExample {json} 验证码错误     
	 * {                                          
	 *     "success": false,                     
	 *     "code": 954,                          
	 *     "msg": "Verification code was wrong",                       
	 *     "data": null                          
	 * }                                          
	 * @apiErrorExample {json} 用户不存在              
	 * {                                          
	 *     "success": false,                     
	 *     "code": 920,                          
	 *     "msg": "Argument invalid",                       
	 *     "data": null                          
	 * }                                          
	 *                                            
	 * @apiUse ErrorExample             
	 *           
	 */
	@RequestMapping("retrieve/password")
	@ResponseBody
	public ResponseModel resetPass(String username, String password, String code, HttpServletRequest request) {
		Date currentDate = new Date();
		if (!StringUtils.isNoneBlank(username, password, code)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		String cacheCode = this.redisTemplate.opsForValue().get(RETRIEVE_VERIFY_CODE_KEY_PREFIX + username);
		if (StringUtils.isBlank(cacheCode)) {
			return super.fail("Verification code has expired", ExceptionCode.BX3);
		}
		if (!StringUtils.equals(cacheCode, code)) {
			return super.fail("Verification code was wrong", ExceptionCode.BX4);
		}
		User user = null;
		if (username.contains("@")) {
			user = this.userService.getByEmail(username);
		} else {
			user = this.userService.getByPhone(username);
		}
		if (user == null) {
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		user.setPassword(TextUtil.md5(password, 3));
		userService.resetPassword(user);
		this.userLogService.addUserLog(currentDate, JsonUtil.toJson(request.getParameterMap()), user.getId(), true, "重置密码", JsonUtil.toJson(super.success()));
		this.redisTemplate.delete(RETRIEVE_VERIFY_CODE_KEY_PREFIX + username);
		return super.success("Password successfully resetted");
	}
	
	
	
	/**
	 * @api {GET} user/get 获取用户信息
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 根据用户id获取用户的详细信息（头像、昵称、手机号、邮箱地址等）
	 * 
	 * @apiParam {String} userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {User} data 用户信息
	 * 
	 * @apiSuccess (User) {String} id 用户id
	 * @apiSuccess (User) {String} phone 手机号
	 * @apiSuccess (User) {String} email 邮箱地址
	 * @apiSuccess (User) {String} nickname 昵称
	 * @apiSuccess (User) {String} avatar 用户头像
	 * @apiSuccess (User) {String} username 用户名
	 * @apiSuccess (User) {String} isDeleted 是否已删除，Y：是，N：否
	 * @apiSuccess (User) {String} registeredDate 注册时间（毫秒级时间戳）
	 * @apiSuccess (User) {Integer} accountType 注册账户类型，1：手机号，2：邮箱
	 * @apiSuccess (User) {Integer} openid sciener用户openid
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data": {
		        "id": "myhzdz6vh9",
		        "phone": "15089601605",
		        "email": "414122112@163.com",
		        "nickname": "代号9527",
		        "avatar": null,
		        "username": "populstay_myhzdz6vh9",
		        "isDeleted": "N",
		        "registeredDate": 1534817967000,
		        "openid": 12334354
		    }
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("get")
	@ResponseBody
	public ResponseModel get(String userId) {
		if (StringUtils.isBlank(userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		User user = this.userService.getById(userId);
		Integer openid = this.scienerService.getOpenid(userId);
		user.setOpenid(openid);
		return super.successData(user);
	}
	
	
	/**
	 * 
	 * @api {POST} user/relogin/push/notice 重登录推送通知
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 用户在另一设备登录时，向前一设备推送下线通知
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {String} deviceId 设备id
	 * @apiParam {String} [apnsToken] ios apns
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("relogin/push/notice")
	@ResponseBody
	@UserLogAnno(title = "重登录通知")
	public ResponseModel pushReloginNotice(String userId, String deviceId, String apnsToken) {
		if (!StringUtils.isNoneBlank(userId, deviceId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		User user = this.userService.getById(userId);
		String content = AbstractController.getText("user.relogin.content");
		this.userMessageService.addUserMessage(userId, AbstractController.getText("user.relogin.title"), content, null);
		this.pushService.send(user, content, 1);
		// 更新登录的设备id
		User modifyUser = new User();
		modifyUser.setId(userId);
		modifyUser.setOptSystem(this.getOptSystem());
		modifyUser.setDeviceId(deviceId);
		apnsToken = apnsToken == null ? "" : apnsToken;
		modifyUser.setApnsToken(apnsToken);
		this.userService.updateUserAndDevice(modifyUser);
		return super.success();
	}
	
	/**
	 * @api {POST} user/delete 删除用户
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 根据用户id删除当前用户信息，用户删除后，可使用同一账号重新注册
	 * 
	 * @apiParam {String} userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": "null"
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} Argument invalid
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	 *
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("delete")
	@ResponseBody
	@UserLogAnno(title = "删除用户")
	public ResponseModel delete(String userId) {
		if (!StringUtils.isNoneBlank(userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		User user = this.userService.getById(userId);
		if (user == null) {
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		if (StringUtils.equals(user.getIsDeleted(), Constants.YES)) {
			return super.success();
		}
		this.userService.deleteUser(user);
		return super.success();
	}
	
	
	/**
	 * @api {POST} user/phone/bind 绑定手机号
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 输入手机号发送验证码（发送验证码接口），输入正确的验证码绑定手机号
	 * 
	 * @apiParam {String} phone 手机号（带国家区号）
	 * @apiParam {String} code 验证码
	 * @apiParam {String} userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Bind successfully",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 验证码已失效
	 * {
		    "success": false,
		    "code": 953,
		    "msg": "Verification code has expired",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 验证码错误
	 * {
		    "success": false,
		    "code": 953,
		    "msg": "Verification code was wrong",
		    "data": null
	   }
	   
	     @apiErrorExample {json} 手机号已被使用
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "The phone number has been used",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("phone/bind")
	@ResponseBody
	@UserLogAnno(title = "绑定手机号")
	public ResponseModel bindPhone(String phone, String code, String userId) {
		if (!StringUtils.isNoneBlank(phone, code, userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		String cacheCode = this.redisTemplate.opsForValue().get(VERIFY_CODE_KEY_PREFIX + phone);
		if (StringUtils.isBlank(cacheCode)) {
			return super.fail("Verification code has expired", ExceptionCode.BX3);
		}
		if (!StringUtils.equals(cacheCode, code)) {
			return super.fail("Verification code was wrong", ExceptionCode.BX4);
		}
		
		User userBean = this.userService.getByPhone(phone);
		if (userBean != null) {
			return super.fail("The phone number has been used", ExceptionCode.BX1);
		}
		
		User user = new User();
		user.setId(userId);
		user.setPhone(phone);
		this.userService.updateUser(user);
		this.redisTemplate.delete(VERIFY_CODE_KEY_PREFIX + phone);
		return super.success("Bind successfully");
	}
	
	/**
	 * @api {POST} user/email/bind 绑定邮箱地址
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 输入邮箱地址发送验证码（发送验证码接口），输入正确的验证码绑定邮箱地址
	 * 
	 * @apiParam {String} email 邮箱地址
	 * @apiParam {String} code 验证码
	 * @apiParam {String} userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Bind successfully",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 验证码已失效
	 * {
		    "success": false,
		    "code": 953,
		    "msg": "Verification code has expired",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 验证码错误
	 * {
		    "success": false,
		    "code": 953,
		    "msg": "Verification code was wrong",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 邮箱已经被使用
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "Email is already in use",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("email/bind")
	@ResponseBody
	@UserLogAnno(title = "绑定邮箱")
	public ResponseModel bindEmail(String email, String code, String userId) {
		if (!StringUtils.isNoneBlank(email, code, userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		String cacheCode = this.redisTemplate.opsForValue().get(VERIFY_CODE_KEY_PREFIX + email);
		if (StringUtils.isBlank(cacheCode)) {
			return super.fail("Verification code has expired", ExceptionCode.BX3);
		}
		if (!StringUtils.equals(cacheCode, code)) {
			return super.fail("Verification code was wrong", ExceptionCode.BX4);
		}
		
		User userBean = this.userService.getByEmail(email);
		if (userBean != null) {
			return super.fail("Email is already in use", ExceptionCode.BX1);
		}
		
		User user = new User();
		user.setId(userId);
		user.setEmail(email);
		this.userService.updateUser(user);
		this.redisTemplate.delete(VERIFY_CODE_KEY_PREFIX + email);
		return super.success("Bind successfully");
	}
	
	/**
	 * @api {POST} user/nickname/modify 修改昵称
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 修改用户昵称
	 * 
	 * @apiParam {String} nickname 用户昵称（app端限制20个字符以内）
	 * @apiParam {String} userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully updated",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("nickname/modify")
	@ResponseBody
	@UserLogAnno(title = "修改昵称")
	public ResponseModel modifyNickname(String userId, String nickname) {
		if (!StringUtils.isNoneBlank(nickname, userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = new User();
		user.setId(userId);
		user.setNickname(nickname);
		this.userService.updateUser(user);
		return super.success("Successfully updated");
	}
	
	/**
	 * @api {POST} user/avatar/upload 上传头像
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 以RFC1867协议上传用户头像，服务端接收到图片文件后转存在服务器本地磁盘上，并映射出相应的http访问地址，上传成功后接口会返回头像的http地址
	 * 
	 * @apiParam {File} file 头像文件
	 * @apiParam {String} userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {String} data 头像地址
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "保存成功",
		    "data": "https://c.tcsyn.cn/files/avatar/fijjlk123i39823jlk_sdjfisjoi23_sdfj9i23.png"
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("avatar/upload")
	@ResponseBody
	@UserLogAnno(title = "上传头像")
	public ResponseModel uploadAvatar(MultipartFile file, String userId) throws IOException {
		if (StringUtils.isBlank(userId) || file == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		String fileUrl = super.uploadFile(file, "avatar");
		User user = new User();
		user.setId(userId);
		user.setAvatar(fileUrl);
		this.userService.updateUser(user);
		return super.successData(fileUrl);
	}

	/**
	 * @api {GET} user/language/switch 语言切换
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 通过参数locale指要切换的目标语言，语言切换后，仅消息和推送通知生效，语言切换后会在客户端一直生效，直到下次切换新的语言
	 * 
	 * @apiParam {String} locale 语种，取值：en_US（英语），zh_CN（简体中文），ja_JP（日语）
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": null
		}
	 * 
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("language/switch")
	@ResponseBody
	public ResponseModel switchLanguage(String locale) {
		return super.success();
	}
	
	/**
	 * 
	 * @api {GET} user/get/deviceId 返回设备id
	 * @apiVersion 1.0.0
	 * @apiGroup UserGroup
	 * @apiDescription 返回设备id, 判断是否已经在异地登陆
	 * 
	 * @apiParam {String} userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data 设备id
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": "1321313"
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 找不到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 *	
	 */
	@RequestMapping("get/deviceId")
	@ResponseBody
	public ResponseModel getDeviceId(String userId) {
		if (StringUtils.isBlank(userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		return super.successData(user.getDeviceId());
	}
	
}
