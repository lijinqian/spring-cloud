/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.igloo.smarthome.model.Keyboard;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.User;
import com.igloo.smarthome.model.vo.KeyboardPwdVo;
import com.igloo.smarthome.model.vo.OperationLogVo;
import com.igloo.smarthome.service.KeyboardService;
import com.igloo.smarthome.service.LockService;
import com.igloo.smarthome.service.OperationLogService;
import com.igloo.smarthome.service.UserService;

import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.model.ExceptionCode;
import tcsyn.basic.model.ResponseModel;
import tcsyn.basic.util.DateUtil;

/**
 * 键盘密码
 * @author lijq
 * @Date 2018年8月25日
 */
@Controller
@RequestMapping("keyboardPwd")
public class KeyboardController extends AbstractController {
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	UserService userService;
	
	@Autowired
	LockService lockService;
	
	@Autowired
	KeyboardService keyboardService;
	
	@Autowired
	OperationLogService operationLogService;
	
	static Logger logger = LoggerFactory.getLogger(KeyboardController.class);
	/**
	 * @apiDefine KeyboardGroup 键盘密码
	 */
	
	/**
	 * @apiDefine SuccessParam
	 * @apiSuccess {boolean} success 本次请求处理结果，true：成功，false：失败
	 * @apiSuccess {Integer} code 请求结果状态码
	 * @apiSuccess {String} msg 提示信息
	 * 
	 */
	
	/**
	 * @api {POST} keyboardPwd/list 键盘密码列表
	 * @apiVersion 1.0.0
	 * @apiGroup KeyboardGroup
	 * @apiDescription 键盘密码列表
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {Integer} pageNo 页码
	 * @apiParam {Integer} pageSize 页面大小
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {List[]} data 返回信息
	 * @apiSuccess (List[]) {Integer} keyboardPwdId 键盘密码id
	 * @apiSuccess (List[]) {String} keyboardPwd 键盘密码
	 * @apiSuccess (List[]) {String} sendUser 发送密码帐号
	 * @apiSuccess (List[]) {String} alias 别名
	 * @apiSuccess (List[]) {Integer} keyboardPwdType 键盘密码类型（参考科技侠平台）
	 * @apiSuccess (List[]) {Long} startDate 生效时间(时间戳)
	 * @apiSuccess (List[]) {Long} endDate 失效时间(时间戳)
	 * @apiSuccess (List[]) {Long} createDate 创建时间(时间戳)
	 * @apiSuccess (List[]) {Integer} status 密码状态(0删除，1未激活，2失效， 3正常)
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data":null
		}
	 * 
	 * @apiErrorExample {json} 用户为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 954,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("list")
	@ResponseBody
	public ResponseModel list(String userId, Integer lockId, Integer pageNo, 
			Integer pageSize) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId) || null == lockId || null == pageNo || null == pageSize) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Lock lock = lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		Boolean isAdmin = false;
		if(StringUtils.equals(userId, lock.getUserId())){
			isAdmin = true;
		}
		
		
		List<KeyboardPwdVo> dataList = keyboardService.list(userId, isAdmin, lockId, (pageNo - 1) * pageSize,  pageSize);

		logger.info("dataList:{}",dataList);
		
		List<OperationLogVo> operList = operationLogService.getLog(lockId);
		logger.info("oper list:{}",operList);
		
		try {

			if( operList.size() <= 0) 
			{
				for(KeyboardPwdVo vo:dataList) {
					vo.setStatus(false,false);
				}
			}
			else 
			{
				Map<String,Boolean> map = new HashMap<String, Boolean>();
				Boolean hasUploaded = true;
				for(OperationLogVo logVo:operList) {
					map.put(logVo.getPassword(), true);
				}
				
				for(KeyboardPwdVo vo:dataList) {
					vo.setStatus(hasUploaded, map.get(vo.getKeyboardPwd()) == null?false:true);
				}
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return super.successData(dataList);
	}
	/**
	 * @api {POST} keyboardPwd/get 获取键盘密码
	 * @apiVersion 1.0.0
	 * @apiGroup KeyboardGroup
	 * @apiDescription 获取键盘密码，是指生成键盘密码
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {Integer} keyboardPwdVersion 键盘密码版本, 三代锁的密码版本为4
	 * @apiParam {Integer} keyboardPwdType 键盘密码类型
	 * @apiParam {String} [startDate] 生效开始时间(永久、单次该值可以为空) （格式：2018-08-12 12:00 或 13:00）
	 * @apiParam {String} [endDate] 失效时间(永久、单次该值可以为空) （格式：2018-08-13 13:00或 14:00）
	 * @apiParam {Integer} [timeZone] 时区
	 * @apiParam {Integer} keyId 钥匙id
	 * @apiParam {String} [alias] 别名
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data": 
		    {
			    "keyboardPwd": "123456",
			    "keyboardPwdId": "10236"
			}
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 954,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("get")
	@ResponseBody
	public ResponseModel get(String userId, Integer lockId, Integer keyboardPwdVersion, Integer keyboardPwdType, 
			String startDate, String endDate, Integer timeZone, Integer keyId, String alias) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId) || null == keyboardPwdType || null == lockId ) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Lock lock = lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		Date currentDate = new Date();
		Date date = this.getPeriodDay(keyboardPwdType, currentDate);
		if (date != null) {
			startDate = DateFormatUtils.format(date, "yyyy-MM-dd ") + startDate;
			endDate = DateFormatUtils.format(date, "yyyy-MM-dd ") + endDate;
		}
		
		Map<String, Object> resultMap = keyboardService.get(user, lock, keyboardPwdVersion, keyboardPwdType,  startDate,  endDate, timeZone, keyId, alias);
		
		if( resultMap.size() <= 0 ) {
			return super.fail("This keyboard password of this lock is existed",ExceptionCode.BX1);
		}
		
		return super.successData(resultMap);
	}
	/**
	 * 
	 * @param keyboardPwdType
	 * @param currentDate
	 * @return
	 */
	private Date getPeriodDay(Integer keyboardPwdType, Date currentDate) {
		Date date = null;
		if (keyboardPwdType == 5) {
			// 周未循环
			int weekdays = DateUtil.getWeekOfDate(currentDate);
			if (weekdays < 6) {
				date = DateUtils.truncate(DateUtils.addDays(currentDate, 6 - weekdays), Calendar.DAY_OF_MONTH);
			} else {
				date = DateUtils.truncate(currentDate, Calendar.DAY_OF_MONTH);
			}
		} else if (keyboardPwdType == 6) {
			// 每日循环
			date = DateUtils.truncate(currentDate, Calendar.DAY_OF_MONTH);
		} else if (keyboardPwdType == 7) {
			// 工作日循环
			int weekdays = DateUtil.getWeekOfDate(currentDate);
			if (weekdays > 5) {
				date = DateUtils.truncate(DateUtils.addDays(currentDate, 8 - weekdays), Calendar.DAY_OF_MONTH);
			} else {
				date = DateUtils.truncate(currentDate, Calendar.DAY_OF_MONTH);
			}
		} else if (keyboardPwdType == 8) {
			// 周一循环，以下递增
			int days = 1;
			date = this.cultsWeekdays(currentDate, days);
		} else if (keyboardPwdType == 9) {
			int days = 2;
			date = this.cultsWeekdays(currentDate, days);
		} else if (keyboardPwdType == 10) {
			int days = 3;
			date = this.cultsWeekdays(currentDate, days);
		} else if (keyboardPwdType == 11) {
			int days = 4;
			date = this.cultsWeekdays(currentDate, days);
		} else if (keyboardPwdType == 12) {
			int days = 5;
			date = this.cultsWeekdays(currentDate, days);
		} else if (keyboardPwdType == 13) {
			int days = 6;
			date = this.cultsWeekdays(currentDate, days);
		} else if (keyboardPwdType == 14) {
			int days = 7;
			date = this.cultsWeekdays(currentDate, days);
		}
		return date;
	}
	/**
	 * 
	 * @param currentDate
	 * @param days
	 * @return
	 */
	private Date cultsWeekdays(Date currentDate, int days) {
		Date date;
		int weekdays = DateUtil.getWeekOfDate(currentDate);
		if (weekdays > days) {
			date = DateUtils.truncate(DateUtils.addDays(currentDate, 7 - weekdays + days), Calendar.DAY_OF_MONTH);
		} else {
			date = DateUtils.truncate(DateUtils.addDays(currentDate, days - weekdays), Calendar.DAY_OF_MONTH);
		}
		return date;
	}
	
	/**
	 * @api {POST} keyboardPwd/delete 删除单个键盘密码
	 * @apiVersion 1.0.0
	 * @apiGroup KeyboardGroup
	 * @apiDescription 删除单个键盘密码
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {Integer} keyboardPwdId 键盘密码ID
	 * @apiParam {Integer} [mediumType] 通讯介质（1：蓝牙，2：网关，默认是1）
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully deleted",
		    "data":null
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到键盘密码
	 * {
		    "success": false,
		    "code": 954,
		    "msg": "Keyboard password not found",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("delete")
	@ResponseBody
	public ResponseModel delete(String userId, Integer lockId, Integer keyboardPwdId, Integer mediumType) throws Exception {
		if(mediumType == null) mediumType = 1;
		
		if (!StringUtils.isNoneBlank(userId) || null == lockId || null == keyboardPwdId ) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Keyboard keyboard = keyboardService.getKeyboardPwd(keyboardPwdId);
		if(null == keyboard){
			return super.fail("Keyboard password not found", ExceptionCode.ENTITY_EMPRY);
		}
		
		keyboardService.delete(user, lockId, keyboardPwdId, mediumType);
		return super.successData("Successfully deleted");
	}
	
	/**
	 * @api {POST} keyboardPwd/add 添加键盘密码
	 * @apiVersion 1.0.0
	 * @apiGroup KeyboardGroup
	 * @apiDescription 添加键盘密码，是指自定义键盘密码
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {String} keyboardPwd 键盘密码
	 * @apiParam {Date} startDate 生效开始时间（格式：2018-08-12 12:00）
	 * @apiParam {Date} endDate 失效时间（格式：2018-08-13 13:00）
	 * @apiParam {Integer} [timeZone] 时区
	 * @apiParam {Integer} keyId 钥匙id
	 * @apiParam {String} [alias] 别名
	 * @apiParam {Integer} [mediumType] 通讯介质（1：蓝牙，2：网关，默认是1）
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data 键盘密码ID
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data":121321
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到键盘密码
	 * {
		    "success": false,
		    "code": 954,
		    "msg": "Keyboard password not found",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("add")
	@ResponseBody
	public ResponseModel add(String userId, Integer lockId, String keyboardPwd,
			String startDate, String endDate, Integer timeZone, Integer keyId, String alias, Integer mediumType) throws Exception {
	
		if(mediumType == null) mediumType = 1;
		
		if (!StringUtils.isNoneBlank(userId) || null == lockId || null == keyboardPwd || null == startDate || null == endDate) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Lock lock = lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		
		Integer count  = keyboardService.checkUsedKeyboards(lockId, keyboardPwd);
		
		if( count > 0 ) {
			return super.fail("This keyboard password of this lock is existed",ExceptionCode.BX1);
		}
		
		Integer keyboardPwdId = keyboardService.add(user, lockId, keyboardPwd, startDate, endDate, timeZone, keyId, alias, mediumType);
		return super.successData(keyboardPwdId);
	}
	
	/**
	 * @api {POST} keyboardPwd/change  修改键盘密码
	 * @apiVersion 1.0.0
	 * @apiGroup KeyboardGroup
	 * @apiDescription 修改键盘密码, 只能修改三代锁， 密码版本为4的密码，支持修改密码和修改密码期限
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {Integer} keyboardPwdId 键盘密码ID
	 * @apiParam {Integer} [newKeyboardPwd] 修改后的密码
	 * @apiParam {String} [startDate] 生效开始时间（格式：2018-08-12 12:00）
	 * @apiParam {String} [endDate] 失效时间（格式：2018-08-13 13:00）
	 * @apiParam {Integer} [timeZone] 时区
	 * @apiParam {Integer} [mediumType] 通讯介质（1：蓝牙，2：网关，默认是1）
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data 返回信息
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully modification",
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
	   
	 * @apiErrorExample {json} 未找到键盘密码
	 * {
		    "success": false,
		    "code": 954,
		    "msg": "Keyboard password not found",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("change")
	@ResponseBody
	public ResponseModel change(String userId, Integer lockId, Integer keyboardPwdId, String newKeyboardPwd, 
			String startDate, String endDate, Integer timeZone, Integer mediumType) throws Exception {
		
		if(mediumType == null) mediumType = 1;
		
		if (!StringUtils.isNoneBlank(userId) || null == lockId || null == keyboardPwdId || null == timeZone) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Lock lock = lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		
		keyboardService.change(user, lockId, keyboardPwdId, newKeyboardPwd, mediumType, startDate, endDate, timeZone);
		return super.success("Successfully modification");
	}
	
	/**
	 * @api {POST} keyboardPwd/changeAlias  修改密码别名
	 * @apiVersion 1.0.0
	 * @apiGroup KeyboardGroup
	 * @apiDescription 修改密码别名
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} keyboardPwdId 键盘密码ID
	 * @apiParam {String} alias 别名
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data 返回信息
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully change the alias of keyboard password",
		    "data":null
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到键盘密码
	 * {
		    "success": false,
		    "code": 954,
		    "msg": "Keyboard password not found",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("changeAlias")
	@ResponseBody
	public ResponseModel changeAlias(String userId, Integer keyboardPwdId, String alias){
		
		if (!StringUtils.isNoneBlank(userId, alias) || null == keyboardPwdId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Keyboard keyboard = keyboardService.getKeyboardPwd(keyboardPwdId);
		if(null == keyboard){
			return super.fail("Not found the keyboard password", ExceptionCode.ENTITY_EMPRY);
		}
		
		keyboardService.changeAlias(keyboardPwdId, alias);
		return super.success("Successfully change the alias of keyboard password");
	}
	
	/**
	 * @api {POST} keyboardPwd/resetKeyboardPwd 重置键盘密码
	 * @apiVersion 1.0.0
	 * @apiGroup KeyboardGroup
	 * @apiDescription 重置键盘密码，用于密码用完后，调用该接口将会生成一批新密码，旧密码全部失效
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {String} pwdInfo 密码数据，用于生成密码，SDK提供
	 * @apiParam {Long} timestamp 时间戳，用于初始化密码数据，SDK提供
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data 返回信息
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data":121321
		}
	 * 
	 * @apiErrorExample {json} 未找到用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 954,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("resetKeyboardPwd")
	@ResponseBody
	public ResponseModel resetKeyboardPwd(String userId, Integer lockId, 
			String pwdInfo, Long timestamp) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId, pwdInfo) || null == lockId || null == timestamp) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Lock lock = lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		
		keyboardService.resetKeyboardPwd(userId, lockId, pwdInfo, timestamp);
		return super.success("Successfully resetted");
	}
	
}
