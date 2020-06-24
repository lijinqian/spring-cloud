/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.model.Key;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.User;
import com.igloo.smarthome.model.vo.KeyDetailVo;
import com.igloo.smarthome.service.KeyService;
import com.igloo.smarthome.service.LockService;
import com.igloo.smarthome.service.UserService;

import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.model.ExceptionCode;
import tcsyn.basic.model.ResponseModel;
import tcsyn.basic.util.DateUtil;

/**
 * 钥匙
 * @author lijq
 * @Date 2018年8月21日
 */
@Controller
@RequestMapping("key")
public class KeyController extends AbstractController {
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	UserService userService;
	
	@Autowired
	LockService lockService;
	
	@Autowired
	KeyService keyService;
	
	
	/**
	 * @apiDefine KeyGroup 钥匙
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
     *		"msg": "服务器运行时遇到了一个错误，错误码：xxxxxx",
     *		"data": null
	 * }
	 * 
	 */
	
	/**
	 * @api {POST} key/send 发送钥匙
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 管理员或被授权用户可以用向其他用户发送钥匙，向同一个用户发送多个钥匙时，只有最后一个钥匙有效
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {String} recUser 接收的帐号
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {Integer} type 钥匙类型（1限时，2永久，3单次）
	 * @apiParam {String} keyAlias 钥匙别名
	 * @apiParam {Date} [startDate] 生效开始时间(永久、单次该值可以为空) （格式：2018-08-12 18:26）
	 * @apiParam {Date} [endDate] 失效时间(永久、单次该值可以为空) （格式：2018-08-12 18:26）
	 * @apiParam {Integer} [timeZone] 时区
	 * @apiParam {Boolean} [arUnlock] 是否允许远程开锁(true/false)
	 * @apiParam {Boolean} [auAdmin] 是否同步授权用户(true/false)
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Sent successfully",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 只能给已注册的用户发送钥匙
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "Not found the receive user",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 管理员不能给自己发送钥匙
	 * {
		    "success": false,
		    "code": 952,
		    "msg": "Can not send a key to self",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 无发送钥匙的权限
	 * {
		    "success": false,
		    "code": 953,
		    "msg": "You did not send key permission",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 超过自己的有效期
	 * {
		    "success": false,
		    "code": 954,
		    "msg": "Can't exceed your own expiration date",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("send")
	@ResponseBody
	public ResponseModel send(String userId, Integer lockId, String recUser, Integer type, 
			String keyAlias, String startDate, String endDate, Integer timeZone, Boolean arUnlock, Boolean auAdmin) throws Exception {
		
		if(null == arUnlock){
			arUnlock = false;
		}
		if(null == auAdmin){
			auAdmin = false;
		}
		
		if (!StringUtils.isNoneBlank(userId, recUser) || null == type) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		User recuser = null;
		if (recUser.contains("@")) {
			recuser = this.userService.getByEmail(recUser);
		} else {
			recuser = this.userService.getByPhone(recUser);
		}
		if (recuser == null) {
			return super.fail("Not found the receive user", ExceptionCode.BX1);
		}
		
		if(StringUtils.equals(userId, recuser.getId())){
			return super.fail("Can not send a key to self", ExceptionCode.BX2);
		}
		
		if(StringUtils.isBlank(keyAlias)){
			keyAlias = recUser;
		}
		Lock lock = lockService.getLockById(lockId);
		if(StringUtils.equals(userId, lock.getUserId())){
			keyService.send(user, recuser, lock, type,  keyAlias,  startDate, endDate, timeZone, arUnlock, auAdmin);
			return super.success("Sent successfully");
		}
		Key key = keyService.getKeyByUserIdAndLockId(userId, lockId);
		if(null == key || Constants.KeyRight.NO == key.getKeyRight()){
			return super.fail("You did not send key permission", ExceptionCode.BX3);
		}
		//限时授权用户只能发送限时钥匙
		if(Constants.KeyType.LIMITTIME == key.getType()){
			if(Constants.KeyType.FOREVER == type){
				return super.fail("You did not send key permission", ExceptionCode.BX5);
			}
			if(Constants.KeyType.LIMITTIME == type){
				final String dateFormat = "yyyy-MM-dd HH:mm";
				Long startTime = DateUtil.getDateTime(startDate, dateFormat, timeZone).getTime() / 1000;
				Long endTime = DateUtil.getDateTime(endDate, dateFormat, timeZone).getTime() / 1000;
				if(startTime < key.getStartDate() || endTime > key.getEndDate()){
					return super.fail("Can't exceed your own expiration date", ExceptionCode.BX4);
				}
			}
		}
		keyService.send(user, recuser, lock, type,  keyAlias,  startDate, endDate, timeZone, arUnlock, auAdmin);
		return super.success("Sent successfully");
		
	}
	
	/**
	 * @api {POST} key/del 删除钥匙
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 删除钥匙,适用于普通用户和授权用户。钥匙列表的删除，普通用户/授权用户在设置页面删除锁的操作
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} keyId 钥匙id
	 * @apiParam {String} delType Y同时删除他所送的钥匙，N则不。（注：只适用于授权用户，普通用户可传空）
	 * 
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully deleted",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到钥匙
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the key",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("del")
	@ResponseBody
	public ResponseModel del(String userId, Integer keyId, String delType) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId) || null == keyId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Key key = this.keyService.getKeyById(keyId);
		if(null == key){
			return super.fail("Not found the key", ExceptionCode.ENTITY_EMPRY);
		}
		
		keyService.del(user, key, delType);
		return super.success("Successfully deleted");
	}
	
	/**
	 * @api {POST} key/freeze 冻结钥匙
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 冻结用户的钥匙，钥匙被冻结后不可以对锁作开锁、闭锁、修改设置等操作
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} keyId 钥匙id
	 * 
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "冻结成功",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到钥匙
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the key",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 待接收的钥匙不能冻结
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "The key to be received cannot be frozen",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("freeze")
	@ResponseBody
	public ResponseModel freeze(String userId, Integer keyId) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId) || null == keyId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Key key = this.keyService.getKeyById(keyId);
		if(null == key){
			return super.fail("Not found the key", ExceptionCode.ENTITY_EMPRY);
		}
		//待接收的钥匙不能冻结
		if(Constants.KeyStatus.WATINGRECEIVE == key.getKeyStatus()){
			return super.fail("The key to be received cannot be frozen", ExceptionCode.BX1);
		}
		
		keyService.freeze(user, key);
		return super.success("Successfully frozen");
	}
	
	/**
	 * @api {POST} key/unfreeze 解冻钥匙
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 解冻钥匙
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} keyId 钥匙id
	 * 
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "解冻成功",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到钥匙
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the key",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("unfreeze")
	@ResponseBody
	public ResponseModel unfreeze(String userId, Integer keyId) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId) || null == keyId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Key key = this.keyService.getKeyById(keyId);
		if(null == key){
			return super.fail("Not found the key", ExceptionCode.ENTITY_EMPRY);
		}
		
		keyService.unfreeze(user, key);
		return super.success("Successfully unfrozen");
	}
	
	/**
	 * @api {POST} key/changePeriod 修改钥匙有效期
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 修改钥匙有效期
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} keyId 钥匙id
	 * @apiParam {Integer} type 钥匙类型（1限时，2永久，3单次）
	 * @apiParam {Date} [startDate] 生效开始时间(永久、单次该值可以为空) （格式：2018.08.12 12:26）
	 * @apiParam {Date} [endDate] 失效时间(永久、单次该值可以为空) （格式：2018.08.12 12:26）
	 * @apiParam {Integer} [timeZone] 时区
	 * 
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "修改有效期成功",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到钥匙
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the key",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("changePeriod")
	@ResponseBody
	public ResponseModel changePeriod(String userId, Integer keyId, Integer type, 
			String startDate, String endDate, Integer timeZone) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId) || null == keyId || null == type) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Key key = this.keyService.getKeyById(keyId);
		if(null == key){
			return super.fail("Not found the key", ExceptionCode.ENTITY_EMPRY);
		}
		
		keyService.changePeriod(user, key, type, startDate, endDate, timeZone);
		return super.success("Successfully modified");
	}
	
	/**
	 * @api {POST} key/authorize 钥匙授权
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 授予普通钥匙用户管理锁的权限，如发送钥匙和获取密码等权限(单次钥匙不能授权)
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} keyId 钥匙id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Authorized success",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到钥匙
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the key",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 只有管理员可以操作
	 * {
		    "success": false,
		    "code": 952,
		    "msg": "Permission denied",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 单次钥匙，不能授权
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "One-time key, cannot be authorized",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 删除或重置状态的钥匙不能授权
	 * {
		    "success": false,
		    "code": 953,
		    "msg": "The key has been reset or deleted. It cannot be authorized",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 待接收的钥匙，不能授权
	 * {
		    "success": false,
		    "code": 953,
		    "msg": "The key to be received.  It cannot be authorized",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 授权的，不能重复授权
	 * {
		    "success": false,
		    "code": 954,
		    "msg": "The key has been authorized and can not be duplicated",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("authorize")
	@ResponseBody
	public ResponseModel authorize(String userId, Integer keyId) {
		
		if (!StringUtils.isNoneBlank(userId) || null == keyId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Key key = this.keyService.getKeyById(keyId);
		if(null == key){
			return super.fail("Not found the key", ExceptionCode.ENTITY_EMPRY);
		}
		
		//删除或重置的钥匙，不能授权
		if(key.getKeyStatus() == Constants.KeyStatus.DELETED || key.getKeyStatus() == Constants.KeyStatus.RESET){
			return super.fail("The key has been reset or deleted. It cannot be authorized", ExceptionCode.BX3);
		}
		
		//待接收的钥匙，不能授权
		if(key.getKeyStatus() == Constants.KeyStatus.WATINGRECEIVE){
			return super.fail("The key to be received.  It cannot be authorized", ExceptionCode.BX4);
		}
		
		//钥匙不能重复授权
		if(key.getKeyRight() == Constants.KeyRight.YES){
			return super.fail("The key has been authorized and can not be duplicated", ExceptionCode.BX4);
		}
		
		Lock lock = lockService.getLockById(key.getLockId());
		if(!StringUtils.equals(userId, lock.getUserId())){
			return super.fail("Permission denied", ExceptionCode.BX2);
		}
		
		if(Constants.KeyType.ONE == key.getType()){
			return super.fail("One-time key, cannot be authorized", ExceptionCode.BX1);
		}
		
		keyService.authorize(user, key);
		return super.success("Authorized success");
	}
	
	/**
	 * @api {POST} key/unauthorize 解除钥匙授权
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 解除授予普通钥匙用户的管理锁的权限
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} keyId 钥匙id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Relieve successfully",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到钥匙
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the key",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("unauthorize")
	@ResponseBody
	public ResponseModel unauthorize(String userId, Integer keyId) {
		
		if (!StringUtils.isNoneBlank(userId) || null == keyId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Key key = this.keyService.getKeyById(keyId);
		if(null == key){
			return super.fail("Not found the key", ExceptionCode.ENTITY_EMPRY);
		}
		
		keyService.unauthorize(user, key);
		return super.success("Relieve successfully");
	}
	
	/**
	 * @api {POST} key/detail 钥匙详情
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 钥匙详情
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} keyId 钥匙id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {data} data 返回信息
	 * 
	 * @apiSuccess (data) {Integer} keyId 钥匙id
	 * @apiSuccess (data) {String} alias 钥匙名称
	 * @apiSuccess (data) {Long} startDate 有效开始时间，0是永久有效 (时间戳)
	 * @apiSuccess (data) {Long} endDate 失效时间，0是永久有效，格式(时间戳)
	 * @apiSuccess (data) {String} recUser 接收帐号
	 * @apiSuccess (data) {String} sendUser 发送帐号
	 * @apiSuccess (data) {Long} sendDate 发送时间(时间戳)
	 * @apiSuccess (data) {String} keyStatus 钥匙的状态（110401：正常使用，110402：待接收，110405：已冻结，110408：已删除，110410：已重置,110500:已过期）
	 * @apiSuccess (data) {Integer} type 有效类型（1限时，2永久，3单次，4循环）
	 * @apiSuccess (data) {String} avatar 用户头像
	 * @apiSuccess (data) {Integer} keyRight 钥匙是否被授权：0-否，1-是
	 * 
	 * 
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * 
	 *  {
	 *      "success": true,
	 *      "code": 200,
	 *      "msg": null,
	 *      "data": {
	 *          "keyId": 1234,
	 *          "alias": "测试",
	 *          "startDate": "1535165505",
	 *          "endDate": "1535165505",
	 *          "recUser": "15089601605",
	 *          "sendUser": "+8613113049377",
	 *          "sendDate": "1535165505",
	 *          "keyStatus": "110402",
	 *          "type": 1
	 *      }
	 *  }
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到钥匙
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the key",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("detail")
	@ResponseBody
	public ResponseModel detail(String userId, Integer keyId) {
		
		if (!StringUtils.isNoneBlank(userId) || null == keyId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		KeyDetailVo vo = this.keyService.getKeyDetail(keyId);
		
		return super.successData(vo);
	}
	
	
	/**
	 * @api {POST} key/list 钥匙列表
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 钥匙列表
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {Integer} pageNo 页码，从1开始
	 * @apiParam {Integer} pageSize 每页数量，默认20，最大10000
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {data} data 返回信息
	 * 
	 * @apiSuccess (data) {Integer} keyId 钥匙id
	 * @apiSuccess (data) {String} alias 钥匙名称
	 * @apiSuccess (data) {Long} startDate 有效开始时间(时间戳)
	 * @apiSuccess (data) {Long} endDate 失效时间，0是永久有效(时间戳)
	 * @apiSuccess (data) {String} recUser 接收帐号
	 * @apiSuccess (data) {String} sendUser 发送帐号
	 * @apiSuccess (data) {Long} sendDate 发送时间(时间戳)
	 * @apiSuccess (data) {String} keyStatus 钥匙的状态（110401：正常使用，110402：待接收，110405：已冻结，110408：已删除，110410：已重置,110500:已过期）
	 * @apiSuccess (data) {Integer} type 有效类型（1限时，2永久，3单次，4循环）
	 * @apiSuccess (data) {String} avatar 用户头像
	 * @apiSuccess (data) {Integer} keyRight 钥匙是否被授权：0-否，1-是
	 * 
	 * 
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * 
	 *  {
	 *      "success": true,
	 *      "code": 200,
	 *      "msg": null,
	 *      "data": [
	 *      	{
	 *             "keyId": 1234,
	 *             "alias": "测试",
	 *             "startDate": "1535165505",
	 *             "endDate": "1535165505",
	 *             "recUser": "15089601605",
	 *             "sendUser": "+8613113049377",
	 *             "sendDate": "1535165505",
	 *             "keyStatus": "110402",
	 *             "type": 1
	 *          },
	 *      	{
	 *      	   "keyId": 456,
	 *             "alias": "测试",
	 *             "startDate": "1535165505",
	 *             "endDate": "1535165505",
	 *             "recUser": "15089601605",
	 *             "sendUser": "+8613113049377",
	 *             "sendDate": "1535165505",
	 *             "keyStatus": "110402",
	 *             "type": 1
	 *          }
	 *      ]
	 *  }
	 * 
	 * @apiErrorExample {json} Argument invalid
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("list")
	@ResponseBody
	public ResponseModel list(String userId, Integer lockId, Integer pageNo, Integer pageSize) {
		if(null == pageSize){
			pageSize = 20;
		}
		
		if (!StringUtils.isNoneBlank(userId) || null == pageNo || lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		Lock lock = this.lockService.getLockById(lockId);
		if (lock == null) {
			return super.fail("LockId invalid", ExceptionCode.ARG_INVALID);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		// 判断用户是否是管理员，管理可以查询锁所有的钥匙，非管理员只能查询自己发送的钥匙
		boolean isAdministrator = false;
		if (StringUtils.equals(lock.getUserId(), userId)) {
			isAdministrator = true;
		}
		List<KeyDetailVo> vos = this.keyService.list(userId, lockId, (pageNo - 1) * pageSize,  pageSize, isAdministrator);
		
		return super.successData(vos);
	}
	
	
	/**
	 * @api {POST} key/changeAlias 修改钥匙别名
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 修改钥匙别名
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} keyId 钥匙id
	 * @apiParam {String} alias 别名
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data 返回信息
	 * 
	 * @apiSuccess (Object) {String} alias 别名
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully modified aliases",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到钥匙
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the key",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("changeAlias")
	@ResponseBody
	public ResponseModel changeAlias(String userId, Integer keyId, String alias) {
		
		if (!StringUtils.isNoneBlank(userId, alias) || null == keyId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Key key = this.keyService.getKeyById(keyId);
		if(null == key){
			return super.fail("Not found the key", ExceptionCode.ENTITY_EMPRY);
		}
		
		key.setAlias(alias);
		keyService.updateKeyById(key);
		
		return super.success("Successfully modified aliases");
	}
	
}
