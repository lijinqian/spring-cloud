/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.model.Key;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.LockAttch;
import com.igloo.smarthome.model.LockPwdInfo;
import com.igloo.smarthome.model.LockVersion;
import com.igloo.smarthome.model.User;
import com.igloo.smarthome.model.vo.ExpireKeyVo;
import com.igloo.smarthome.model.vo.LockUserVo;
import com.igloo.smarthome.model.vo.LockVo;
import com.igloo.smarthome.service.KeyService;
import com.igloo.smarthome.service.LockAttchService;
import com.igloo.smarthome.service.LockHomeService;
import com.igloo.smarthome.service.LockPwdInfoService;
import com.igloo.smarthome.service.LockService;
import com.igloo.smarthome.service.UserService;

import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.model.ExceptionCode;
import tcsyn.basic.model.ResponseModel;
import tcsyn.basic.util.TextUtil;

/**
 * 锁
 * @author lijq
 * @Date 2018年8月21日
 */
@Controller
@RequestMapping("lock")
public class LockController extends AbstractController {
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	UserService userService;
	
	@Autowired
	LockService lockService;
	
	@Autowired
	LockAttchService lockAttchService;
	
	@Autowired
	LockPwdInfoService lockPwdInfoService;
	
	@Autowired
	KeyService keyService;
	
	@Autowired
	LockHomeService lockHomeService;
	
	
	/**
	 * @apiDefine LockGroup 锁
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
	 * @apiIgnore
	 * @api {POST} lock/init 初始化锁
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 初始化锁,(谁初始化锁，谁就是该锁的管理员。初始化后，之前所有的钥匙密码失效)
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {String} name 锁的蓝牙名称
	 * @apiParam {String} alias 锁别名
	 * @apiParam {String} mac 锁mac地址
	 * @apiParam {String} key 锁开门的关键信息，开门用的
	 * @apiParam {Integer} flagPos 锁开门标志位
	 * @apiParam {String} aesKey Aes加解密key
	 * @apiParam {String} adminPwd 理员钥匙会有，锁的管理员密码，锁管理相关操作需要携带，校验管理员权限
	 * @apiParam {String} noKeyPwd 管理员键盘密码
	 * @apiParam {String} deletePwd 二代锁的管理员钥匙会有，清空码
	 * @apiParam {String} pwdInfo 密码数据，用于生成密码，SDK提供
	 * @apiParam {String} timestamp 时间戳，用于初始化密码数据，SDK提供
	 * @apiParam {Integer} specialValue 锁特征值，用于表示锁支持的功能
	 * @apiParam {Integer} electricQuantity 锁电量
	 * @apiParam {String} timezoneRawOffSet 锁所在时区和UTC时区时间的差数，单位milliseconds
	 * @apiParam {String} modelNum 产品型号
	 * @apiParam {String} hardwareRevision 硬件版本号
	 * @apiParam {Integer} protocolType 协议类型
	 * @apiParam {Integer} protocolVersion 协议版本
	 * @apiParam {Integer} scene 场景
	 * @apiParam {Integer} groupId 公司
	 * @apiParam {Integer} orgId 应用商
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {data} data 返回信息
	 * 
	 * @apiSuccess (data) {Integer} lockId 科技侠锁id
	 * @apiSuccess (data) {Integer} keyId 科技侠管理员钥匙id
	 * 
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data": {
		    	"id":"id",
		    	"lockId":"121234",
		    	"keyId":"32323"
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
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("init")
	@ResponseBody
	public ResponseModel init(Lock lock, LockAttch lockAttch, LockPwdInfo lockPwdInfo, LockVersion lockVersion) throws Exception {
		
		User user = this.userService.getUserById(lock.getUserId());
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		if (StringUtils.equals(Constants.YES, user.getIsDeleted())) {
			return super.fail("Invalid user identify", ExceptionCode.ARG_INVALID);
		}
		if(StringUtils.isBlank(lock.getAlias())){
			lock.setAlias(lock.getName());
		}
//		try{
			Map<String,Object> result = lockService.init(lock, lockAttch, lockPwdInfo, lockVersion);
			return super.successData(result);
//		}catch(SystemException e){
//			e.getCause().printStackTrace();
//			return super.su
//		}
		
	}
	/**
	 * @api {POST} lock/init/v3 初始化锁
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 初始化锁,(谁初始化锁，谁就是该锁的管理员。初始化后，之前所有的钥匙密码失效)
	 * 
	 * @apiParam {String} lockData 锁数据，由SDK生成
	 * @apiParam {String} mac 锁mac地址
	 * @apiParam {String} name 锁名
	 * @apiParam {String} [lockAlias] 锁别名
	 * @apiParam {String} userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {data} data 返回信息
	 * 
	 * @apiSuccess (data) {Integer} lockId 科技侠锁id
	 * @apiSuccess (data) {Integer} keyId 科技侠管理员钥匙id
	 * 
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data": {
		    	"id":"id",
		    	"lockId":"121234",
		    	"keyId":"32323"
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
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("init/v3")
	@ResponseBody
	public ResponseModel init_v3(String lockData, String mac, String name, String lockAlias, String userId) throws Exception {
		
		if(!StringUtils.isNoneBlank(lockData, mac, name, userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		if (StringUtils.equals(Constants.YES, user.getIsDeleted())) {
			return super.fail("Invalid user identify", ExceptionCode.ARG_INVALID);
		}
		if(StringUtils.isBlank(lockAlias)){
			lockAlias = name;
		}
		Map<String,Object> result = lockService.init_v3(lockData, mac, name, lockAlias, userId);
		return super.successData(result);
		
	}
	
	/**
	 * @api {POST} lock/list 锁列表
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 锁列表,实指该用户所拥有的钥匙列表
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} pageNo 页码，从1开始
	 * @apiParam {Integer} pageSize 每页数量，默认20，最大10000
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {data} data 返回信息
	 * 
	 * @apiSuccess (data) {String} lockAlias 锁别名
	 * @apiSuccess (data) {Integer} keyId 钥匙id
	 * @apiSuccess (data) {String} keyAlias 钥匙名称
	 * @apiSuccess (data) {Long} startDate 有效开始时间(时间戳)
	 * @apiSuccess (data) {Long} endDate 失效时间，0是永久有效(时间戳)
	 * @apiSuccess (data) {String} keyStatus 钥匙的状态（110401：正常使用，110402：待接收，110405：已冻结，110408：已删除，110410：已重置,110500:已过期）
	 * @apiSuccess (data) {Integer} type 有效类型（1限时，2永久，3单次，4循环）
	 * @apiSuccess (data) {String} userType 钥匙用户类型：110301-管理员钥匙，110302-普通用户钥匙
	 * @apiSuccess (data) {Integer} keyRight 钥匙是否被授权：0-否，1-是
	 * @apiSuccess (data) {Integer} dayNum 剩余有效天数
	 * @apiSuccess (data) {Integer} electricQuantity 锁电量
	 * 
	 * 
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * 
		{
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": [
		        {
		            "lockAlias": "gghh",
		            "keyAlias": "我自己5s号码",
		            "keyId": 2196444,
		            "startDate": 1537242000,
		            "endDate": 1537328400,
		            "keyStatus": "110401",
		            "type": 1,
		            "userType": "110302",
		            "keyRight": 0,
		            "dayNum": 0,
		            "electricQuantity": 12
		        }
		    ]
		}
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
	public ResponseModel list(String userId, Integer pageNo, Integer pageSize) {
		if(null == pageSize){
			pageSize = 20;
		}
		
		if (!StringUtils.isNoneBlank(userId) || null == pageNo) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		List<LockVo> vos = this.lockService.list(userId, (pageNo - 1) * pageSize,  pageSize);
		
		return super.successData(vos);
	}
	
	
	/**
	 * @api {POST} lock/del 删除锁
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 删除锁(只有管理员可以操作)
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {String} password 用户密码
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {data} data 返回信息
	 * 
	 * @apiSuccess (data) {Integer} lockId 科技侠锁id
	 * @apiSuccess (data) {Integer} keyId 科技侠管理员钥匙id
	 * 
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
	   
	 * @apiErrorExample {json} 密码错误
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "Password was wrong",
		    "data": null
	   }
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 没有权限
	 * {
		    "success": false,
		    "code": 952,
		    "msg": "Permission denied",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("del")
	@ResponseBody
	public ResponseModel del(String userId, Integer lockId, String password) throws Exception {
		if (!StringUtils.isNoneBlank(userId, password) || null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		if(!StringUtils.equals(user.getPassword(), TextUtil.md5(password, 3))){
			return super.fail("Password was wrong", ExceptionCode.BX1);
		}
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		if (!StringUtils.equals(lock.getUserId(), userId)) {
			return super.fail("Permission denied", ExceptionCode.BX2);
		}
		// 管理员删除锁
		this.lockService.deleteLock4Manager(userId, lock, lock.getKeyId(), true);
		/* else {
			// 普通用户或者授权用户删除锁
			this.lockService.deleteLock4User(userId, lock);
		}*/
		return super.success("Successfully deleted");
	}
	
	/**
	 * @api {POST} lock/deleteAllKey 删除锁下所有普通钥匙
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 删除锁下所有普通钥匙，（注：管理员钥匙有效）
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
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
	 * @apiErrorExample {json} 参数为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
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
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("deleteAllKey")
	@ResponseBody
	public ResponseModel deleteAllKey(String userId, Integer lockId) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId) || null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		
		lockService.deleteAllKey(user, lock);
		return super.success("Successfully deleted");
	}
	
	/**
	 * @api {POST} lock/specialValue/set 修改锁锁特征值
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 修改锁锁特征值
	 * 
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {Integer} specialValue 锁特征值
	 * 
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully resetted",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	 *	
	 */
	@RequestMapping("specialValue/set")
	@ResponseBody
	public ResponseModel setSpecialValue(Integer lockId, Integer specialValue) throws Exception {
		
		if (null == lockId || null == specialValue) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		LockAttch lockAttch = this.lockAttchService.getLockAttchById(lockId);
		if(null == lockAttch){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		lockAttch.setSpecialValue(specialValue);
		lockAttchService.updateSpecialValue(lockAttch);
		return super.success("Successfully resetted");
	}
	/**
	 * @api {POST} lock/resetKey 重置普通钥匙
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 重置普通钥匙(只允许管理员操作，必需先通过SDK重置钥匙，再调用该接口)
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * 
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully resetted",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
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
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 没有操作的权限
	 * {
		    "success": false,
		    "code": 910,
		    "msg": "No permission",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("resetKey")
	@ResponseBody
	public ResponseModel resetKey(String userId, Integer lockId) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId) || null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		
		if(!StringUtils.equals(userId, lock.getUserId())){
			return super.fail("No permission", ExceptionCode.BX1);
		}
		lockService.resetKey(user, lock);
		return super.success("Successfully resetted");
	}
	
	/**
	 * @api {POST} lock/cleanKey 清空钥匙
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 清空钥匙
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * 
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully resetted",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
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
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 没有操作的权限
	 * {
		    "success": false,
		    "code": 910,
		    "msg": "No permission",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("cleanKey")
	@ResponseBody
	public ResponseModel cleanKey(String userId, Integer lockId) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId) || null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		Boolean isAdmin = false;
		Key key = keyService.getKeyByUserIdAndLockId(userId, lockId);
		if(null == key || !(StringUtils.equals(Constants.KeyUserType.MANGER, key.getUserType()) || Constants.KeyRight.YES == key.getKeyRight())){
			return super.fail("No permission", ExceptionCode.BX1);
		}else{
			isAdmin = true;
		}
		
		lockService.cleanKey(user, lock, isAdmin);
		return super.success("Successfully cleaned");
	}
	
	/**
	 * @api {POST} lock/changeDeletePwd 修改锁的清空密码
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 修改锁的清空密码
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {String} deletePwd 清空密码
	 * 
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data": "deletePwd"
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
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
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("changeDeletePwd")
	@ResponseBody
	public ResponseModel changeDeletePwd(String userId, Integer lockId, String deletePwd) throws Exception {
		
		if (!StringUtils.isNoneBlank(userId, deletePwd) || null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		
		LockPwdInfo lockPwdInfo = this.lockPwdInfoService.getLockPwdInfoById(lockId);
		if(null == lockPwdInfo){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		lockPwdInfo.setDeletePwd(deletePwd);
		lockPwdInfoService.changeDeletePwd(user, lockPwdInfo);
		return super.successData(deletePwd);
	}


	/**
	 * @api {POST} lock/rename 修改锁名称
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 修改锁名称
	 * 
	 * @apiParam {Integer}  lockId    锁id
	 * @apiParam {String}  lockAlias 锁别名
	 * 
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Modify successfully",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("rename")
	@ResponseBody
	public ResponseModel rename(Integer lockId, String lockAlias) throws Exception {
		if (!StringUtils.isNoneBlank(lockAlias) || null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		lock.setAlias(lockAlias);
		lockService.rename(lock);
		return super.success("Modify successfully");
	}
	
	/**
	 * @api {POST} lock/updateElectricQuantity 上传锁电量
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 上传锁电量
	 * 
	 * @apiParam {Integer}  lockId   锁id
	 * @apiParam {String}  electricQuantity 锁电量
	 * 
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully uploaded",
		    "data": null
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("updateElectricQuantity")
	@ResponseBody
	public ResponseModel updateElectricQuantity(Integer lockId, String electricQuantity) throws Exception {
		if (!StringUtils.isNoneBlank(electricQuantity) || null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		LockAttch lockAttch = this.lockAttchService.getLockAttchById(lockId);
		if(null == lockAttch){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		lockAttch.setElectricQuantity(Integer.parseInt(electricQuantity));
		lockAttchService.updateElectricQuantity(lockAttch);
		return super.success("Successfully uploaded");
	}
	
	
	/**
	 * @api {GET} lock/get 获取当前用户的锁
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 获取用户id（锁管理员）下管理的锁
	 * 
	 * @apiParam {String}  userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Lock[]} data 锁信息列表
	 * 
	 * @apiSuccess (Lock) {String} lockId 科技侠的锁id
	 * @apiSuccess (Lock) {String} userId 用户id
	 * @apiSuccess (Lock) {String} name 锁的蓝牙名称
	 * @apiSuccess (Lock) {String} alias 锁别名
	 * @apiSuccess (Lock) {String} mac 锁mac地址
	 * @apiSuccess (Lock) {String} key 锁开门的关键信息，开门用的
	 * @apiSuccess (Lock) {Integer} flagPos 锁开门标志位
	 * @apiSuccess (Lock) {String} aesKey Aes加解密key
	 * @apiSuccess (Lock) {String} adminPwd 管理员钥匙会有，锁的管理员密码，锁管理相关操作需要携带，校验管理员权限
	 * @apiSuccess (Lock) {String} noKeyPwd 管理员键盘密码
	 * @apiSuccess (Lock) {String} pwdInfo 密码数据，用于生成密码，SDK提供
	 * @apiSuccess (Lock) {Long} timestamp 时间戳，用于初始化密码数据
	 * @apiSuccess (Lock) {Integer} specialValue 锁特征值，用于表示锁支持的功能
	 * @apiSuccess (Lock) {Integer} electricQuantity 锁电量
	 * @apiSuccess (Lock) {Long} timezoneRawOffSet 锁所在时区和UTC时区时间的差数，单位milliseconds
	 * @apiSuccess (Lock) {String} modelNum 产品型号（用于锁固件升级）
	 * @apiSuccess (Lock) {String} hardwareRevision 硬件版本号（用于锁固件升级）
	 * @apiSuccess (Lock) {String} firmwareRevision 固件版本号（用于锁固件升级）
	 * @apiSuccess (Lock) {Long} initDate 初始化时间
	 * @apiSuccess (Lock) {String} keyId 管理员钥匙id
	 * @apiSuccess (Lock) {Integer} status 锁状态（0删除，1正常）
	 * 
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully uploaded",
		    "data": [
		    	{
		            "lockId": 1264371,
		            "userId": "4hi463iikx",
		            "name": "IGB3-C2A4P1_d2fe07",
		            "alias": "IGB3-C2A4P1_d2fe07",
		            "mac": "C4:F2:7E:07:FE:D2",
		            "key": "MTAyLDExMCw5NiwxMDIsOTYsMTAzLDk5LDk5LDExMCwxMDIsNDA=",
		            "flagPos": 0,
		            "aesKey": "3c,eb,f9,05,da,fd,22,c0,7c,f7,58,a3,5c,b2,73,3c",
		            "adminPwd": "Niw0LDIsNSwxNCwwLDMsNCw1LDAsNzI=",
		            "noKeyPwd": "1175242",
		            "deletePwd": "",
		            "pwdInfo": "RAwVrygGEOJf462PzSu85OH2f6LeoL7HUaXTJQUSNDgO/rilw67m9kqTDAG9ZhwK9wiKaUAAnuqBSbPL9Dx+uKc2npNM/OmkIv0ktSerwV0kxniDs2FvcOe2mXOGg7ekkfFpdwQMREGbjms/9PI3KAY/4YWIC2YtGxvy3zEIjV9z+eN45IG4TWAqkjp+e+glhU1N0Uugur+ZEwKQiOzF1cN5XRe6EITvtiH+czUNMpvt8xbpc5Fa6TznruCi8mfVVX9QN+gYgZS8ehXEjwo9PX3smXwkfCUhzSuKevOAycRws00oMNiJKqGaYOm9mPtDfNzF5HLXXmj4WPc9jEvilW33V5LGmVwTYB9geUdnA7G+tGB5QwFYWgxYsY19hDOGvj+nBzzIq3nGeK9wldllI0gVlrjxeHPWJxKYu8442KaKtMqh9fyVpJBnbMDzahHi4celaOd4NgGi0dfMZLEU0eXI1yj8A5l1VUeUBriNHrAteJjwrkaPccv4h33shSuIEwRz0p1GU+wcCx/a6/TVxFwqJB/rY9LQj7c9o3ihl2mL4Ip9mFB7qsc2F0ZqVN3eRdWbsO6X3Mz3iQ+lM50x+ApiIu8TxycFVfATrw1oI6sZaqyzAdZpHP1QqHOBRyVVSVJc1GhTE5a4Pyb8l0e7irecto4WVzbOvbQribqHVQlnwE/xVAcq6s9JQpltoTsJWydml5iYQJeKcr/tYR8Qm+hiePSKUCgsHGItrGZYmD4=",
		            "timestamp": 1535030058500,
		            "specialValue": 12785,
		            "electricQuantity": 67,
		            "timezoneRawOffSet": 28800000,
		            "modelNum": "SN138-IGB3_PV53",
		            "hardwareRevision": "1.2",
		            "firmwareRevision": "4.1.18.0117",
		            "initDate": 1535030069000,
		            "keyId": 2008595,
		            "status": 1
		        }
		    ]
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("get")
	@ResponseBody
	public ResponseModel get(String userId) {
		if (!StringUtils.isNoneBlank(userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		List<Lock> lockList = this.lockService.getByUserId(userId);
		return super.successData(lockList);
	}
	
	/**
	 * @api {POST} lock/queryDate 读取锁时间
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 读取锁时间
	 * 
	 * @apiParam {Integer}  lockId  锁id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {data} data 返回信息
	 * 
	 * @apiSuccess (data) {Long} date 锁时间戳
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data": 
		    {
			    "date": 1490234647000
			}
		}
	 * 
	 * @apiErrorExample {json} 参数为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("queryDate")
	@ResponseBody
	public ResponseModel queryDate(Integer lockId) throws Exception {
		if (null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		Map<String, Object> queryDate = lockService.queryDate(lock);
		return super.successData(queryDate);
	}
	
	
	/**
	 * @api {get} lock/baseInfo/get 获取锁信息
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 获取锁信息
	 * 
	 * @apiParam {String}  userId  用户id
	 * @apiParam {Integer}  lockId  锁id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Lock} data 返回信息
	 * 
	 * @apiSuccess (Lock) {String} lockId 科技侠的锁id
	 * @apiSuccess (Lock) {String} userId 用户id
	 * @apiSuccess (Lock) {String} name 锁的蓝牙名称
	 * @apiSuccess (Lock) {String} alias 锁别名
	 * @apiSuccess (Lock) {String} mac 锁mac地址
	 * @apiSuccess (Lock) {String} key 锁开门的关键信息，开门用的
	 * @apiSuccess (Lock) {Integer} flagPos 锁开门标志位
	 * @apiSuccess (Lock) {String} aesKey Aes加解密key
	 * @apiSuccess (Lock) {String} adminPwd 管理员钥匙会有，锁的管理员密码，锁管理相关操作需要携带，校验管理员权限
	 * @apiSuccess (Lock) {String} noKeyPwd 管理员键盘密码
	 * @apiSuccess (Lock) {String} pwdInfo 密码数据，用于生成密码，SDK提供
	 * @apiSuccess (Lock) {Long} timestamp 时间戳，用于初始化密码数据
	 * @apiSuccess (Lock) {Integer} specialValue 锁特征值，用于表示锁支持的功能
	 * @apiSuccess (Lock) {Integer} electricQuantity 锁电量
	 * @apiSuccess (Lock) {Long} timezoneRawOffSet 锁所在时区和UTC时区时间的差数，单位milliseconds
	 * @apiSuccess (Lock) {String} modelNum 产品型号（用于锁固件升级）
	 * @apiSuccess (Lock) {String} hardwareRevision 硬件版本号（用于锁固件升级）
	 * @apiSuccess (Lock) {String} firmwareRevision 固件版本号（用于锁固件升级）
	 * @apiSuccess (Lock) {Long} initDate 初始化时间
	 * @apiSuccess (Lock) {String} keyId 管理员钥匙id
	 * @apiSuccess (Lock) {Integer} status 锁状态（0删除，1正常）
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
		{
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": {
		        "lockId": 1264371,
		        "userId": "4hi463iikx",
		        "name": "IGB3-C2A4P1_d2fe07",
		        "alias": "哈哈哈",
		        "mac": "C4:F2:7E:07:FE:D2",
		        "key": "NTUsNjIsNTQsNTIsNTEsNTQsNTUsNTQsNTIsNjIsMTIx",
		        "flagPos": 0,
		        "aesKey": "46,33,8b,55,63,86,b1,08,1b,bd,71,1a,8d,44,7a,33",
		        "adminPwd": "MTIsNSwxNCw1LDksMTIsMTEsMTUsMTIsOCw2Ng==",
		        "noKeyPwd": "5497551",
		        "deletePwd": "",
		        "pwdInfo": "C9InLBLfFNZpciJ2dGyegwcGeU/9wXc14BJp13UtTszfS8MfdUSWs4BPHPl1HlamtlY47cTpSL0rXQ06PHIW+AGVY4hZWhp/p7CFNVpZFcwz+m4tGbnv/pRZVwNzFPpTpJT4Vl2kf/qcXj6Gx+bwEBWPB5joStA2PDpO5R0vl5VDGNFlMm04AyBKJ2l0N0CpzseKYLke1W/vFr1mJ2u2zhcocQPN4zXEQ3ll5U8mGnhaRlhHyGF/55iOABXw2aznhv2oENsbjiG5jhVKpWEvMi7p1pKYg6CkzTFNOVVq+YqbxDB0jLfTwgqTFEroSnxEObLfq4AA/29lb+gCpYoOh01112qudYyB6MBv2/+cazJizN9wpie02GgMOwdjibjIiAZvARHVEixGdwcHkyp6rosTUeuOPVm7DqTEpmeSOp3Fhj2x59G66rxAqMq+vTHDGwnj9iYVuabYh3BbmJR5m6E7CGbVEETyj+AojPnyH8WJpS2lZJjJJWOPxz/M4ZMBIEfG0f2RVRVJwDPg6Wx11tpVhs9zAPi9aRkAhpc5pI4kiD/IVMMn5jIT4BtwJAef7F6kcgqjYoA7wTfvU13uaHbr/nVmNip0D/asLXx1i6+aWAyK/arv53A2fUAmr2Dl55n6/TdmK/myeAdhZHebMf1s/aqkk1f/mRrrq6KoHK5v3sbRlUc4/JKZN3xGyVi9TVXLxApgheLnGu6f0LTiig==",
		        "timestamp": 1535194494217,
		        "specialValue": 12785,
		        "electricQuantity": 67,
		        "timezoneRawOffSet": 28800000,
		        "modelNum": "SN138-IGB3_PV53",
		        "hardwareRevision": "1.2",
		        "firmwareRevision": "4.1.18.0117",
		        "initDate": 1535194496000,
		        "keyId": 2016729,
		        "status": 1
		    }
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} Lock has been deleted
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Lock has been deleted",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("baseInfo/get")
	@ResponseBody
	public ResponseModel getBaseInfo(String userId, Integer lockId) throws Exception {
		if (StringUtils.isBlank(userId)|| null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		User user = this.userService.getUserById(userId);
		if(null == user){
			return super.fail("Argument invalid", ExceptionCode.ENTITY_EMPRY);
		}
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		if(Constants.LockStatus.DELETED == lock.getStatus()){
			return super.fail("Lock has been deleted", ExceptionCode.ENTITY_EMPRY);
		}
		return super.successData(lock);
	}
	
	/**
	 * @api {POST} lock/updateDate 校准锁时间
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 读取锁时间
	 * 
	 * @apiParam {Integer}  lockId  锁id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {data} data 返回信息
	 * 
	 * @apiSuccess (data) {Integer} date 校准后锁上的时间
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data": 
		    {
			    "date": 1490234647000
			}
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("updateDate")
	@ResponseBody
	public ResponseModel updateDate(Integer lockId) throws Exception {
		if (null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		Map<String, Object> updateDate = lockService.updateDate(lock);
		return super.successData(updateDate);
	}
	
	/**
	 * @apiIgnore[暂时不做]
	 * @api {POST} lock/transfer 转移锁
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 转移锁
	 * 
	 * @apiParam {String}  userId  用户id
	 * @apiParam {String}  recUser  接收用户
	 * @apiParam {String}  lockIdList  锁id列表(格式：1234,3332)
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {data} data 返回信息
	 * 
	 * @apiSuccess (data) {Integer} date 校准后锁上的时间
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data": 
		    {
			    "date": 1490234647000
			}
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 无效用户
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Argument invalid",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("transfer")
	@ResponseBody
	public ResponseModel transfer(String userId, String recUser, String lockIdList) throws Exception {
		if (StringUtils.isAnyEmpty(userId, recUser, lockIdList)) {
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
		
		lockService.transfer(user, recuser, lockIdList);
		return super.success();
	}
	
	/**
	 * @api {POST} lock/user/list 锁用户管理列表
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 锁用户管理列表
	 * 
	 * @apiParam {String}  userId  用户id
	 * @apiParam {Integer}  lockId  锁id
	 * @apiParam {Integer} pageNo 页码，从1开始
	 * @apiParam {Integer} pageSize 每页数量，默认20，最大10000
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {data[]} data 返回信息
	 * 
	 * @apiSuccess (data[]) {String} avatar 用户头像
	 * @apiSuccess (data[]) {String} userId 用户id
	 * @apiSuccess (data[]) {String} nickname 昵称>手机>邮箱
	 * @apiSuccess (data[]) {String} userName 用户帐号
	 * @apiSuccess (data[]) {Integer} keyId 钥匙id
	 * @apiSuccess (data[]) {String} alias 钥匙名称
	 * @apiSuccess (data[]) {Long} startDate 有效开始时间(时间戳)
	 * @apiSuccess (data[]) {Long} endDate 失效时间，0是永久有效(时间戳)
	 * @apiSuccess (data[]) {String} recUser 接收帐号
	 * @apiSuccess (data[]) {String} sendUser 发送帐号
	 * @apiSuccess (data[]) {Long} sendDate 发送时间(时间戳)
	 * @apiSuccess (data[]) {String} keyStatus 钥匙的状态（110401：正常使用，110402：待接收，110405：已冻结，110408：已删除，110410：已重置,110500:已过期）
	 * @apiSuccess (data[]) {Integer} type 有效类型（1限时，2永久，3单次，4循环）
	 * @apiSuccess (data[]) {String} avatar 用户头像
	 * @apiSuccess (data[]) {Integer} keyRight 钥匙是否被授权：0-否，1-是
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "",
		    "data":
		     [ 
			     {
		            "avatar": "https://c.tcsyn.cn/files/avatar/4nltwbomij.png",
		            "userId": "sas",
		            "nickname": "twt",
		            "userName": "15089601605"
		        }
	        ]
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 只有管理员有权限操作
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "Permission denied",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("user/list")
	@ResponseBody
	public ResponseModel getUserList(String userId, Integer lockId, Integer pageNo, Integer pageSize) throws Exception {
		if(null == pageSize){
			pageSize = 20;
		}
		if (StringUtils.isBlank(userId) || null == lockId || null == pageNo) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		// 判断用户是否是管理员
		if (!StringUtils.equals(lock.getUserId(), userId)) {
			return super.fail("Permission denied", ExceptionCode.BX1);
		}
		List<LockUserVo> dataList = lockService.getUserList(lockId, userId,  (pageNo - 1) * pageSize, pageSize);
		return super.successData(dataList);
	}
	
	/**
	 * @api {POST} lock/expire/key/list 即将到期钥匙列表
	 * @apiVersion 1.0.0
	 * @apiGroup KeyGroup
	 * @apiDescription 即将到期钥匙列表
	 * 
	 * @apiParam {String}  userId  用户id
	 * @apiParam {Integer}  lockId  锁id
	 * @apiParam {Integer} pageNo 页码，从1开始
	 * @apiParam {Integer} pageSize 每页数量，默认20，最大10000
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {list[]} data 返回信息
	 * @apiSuccess (list[]) {String} avatar 头像
	 * @apiSuccess (list[]) {String} nickname 昵称>手机>邮箱
	 * @apiSuccess (list[]) {Long} startDate 生效开始时间(时间戳)
	 * @apiSuccess (list[]) {Long} endDate 失效时间(时间戳)
	 * @apiSuccess (list[]) {String} lockAlias 锁别名
	 * @apiSuccess (list[]) {Integer} dayNum 剩余有效天数
	 * @apiSuccess (list[]) {String} keyStatus 钥匙的状态（110401：正常使用，110402：待接收，110405：已冻结，110408：已删除，110410：已重置,110500:已过期）
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
		{
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": [
		        {
		            "avatar": null,
		            "nickname": "twt",
		            "startDate": 12335354343,
		            "endDate": 12335354343,
		            "lockAlias": "哈哈哈",
		            "dayNum": 1,
		            "keyStatus": "110401"
		        },
		        {
		            "avatar": null,
		            "nickname": "twt",
		            "startDate": 12335354343,
		            "endDate": 12335354343,
		            "lockAlias": "哈哈哈",
		            "dayNum": 1,
		            "keyStatus": "110401"
		        }
		    ]
		}
	 * 
	 * @apiErrorExample {json} 参数不能为空
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiErrorExample {json} 未找到锁
	 * {
		    "success": false,
		    "code": 920,
		    "msg": "Not found the lock",
		    "data": null
	   }
	 * @apiErrorExample {json} 只有管理员有权限操作
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "Permission denied",
		    "data": null
	   }
	   	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("expire/key/list")
	@ResponseBody
	public ResponseModel getExpireKeyList(String userId, Integer lockId, Integer pageNo, Integer pageSize) throws Exception {
		if(null == pageSize){
			pageSize = 20;
		}
		if (StringUtils.isBlank(userId) || null == lockId || null == pageNo) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		// 判断用户是否是管理员
		if (!StringUtils.equals(lock.getUserId(), userId)) {
			return super.fail("Permission denied", ExceptionCode.BX1);
		}
		
		List<ExpireKeyVo> dataList = lockService.getExpireKeyList(lockId, userId,  (pageNo - 1) * pageSize, pageSize);
		return super.successData(dataList);
	}
	
	/**
	 * @api {POST} lock/bind-home 绑定家庭
	 * @apiVersion 1.0.0
	 * @apiGroup LockGroup
	 * @apiDescription 将锁绑定到一个家庭
	 * 
	 * @apiParam {Integer}  lockId  锁id
	 * @apiParam {String}  homeId 家庭id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
		{
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": null
		}
	 * @apiUse ErrorExample
	 */
	@RequestMapping("bind-home")
	@ResponseBody
	public ResponseModel bindHome(Integer lockId, String homeId) {
		if (StringUtils.isBlank(homeId) || null == lockId) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Lock lock = this.lockService.getLockById(lockId);
		if(null == lock){
			return super.fail("Not found the lock", ExceptionCode.ENTITY_EMPRY);
		}
		this.lockHomeService.addLockHome(lockId, homeId);
		return super.success();
	}
}
