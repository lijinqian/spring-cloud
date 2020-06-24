/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.constant.Constants;
import com.igloo.smarthome.ext.ScienerService;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.LockAttch;
import com.igloo.smarthome.service.GatewayService;
import com.igloo.smarthome.service.LockAttchService;
import com.igloo.smarthome.service.LockService;
import com.igloo.smarthome.service.OperationLogService;
import com.igloo.smarthome.service.UserMessageService;

import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.ext.SystemException;
import tcsyn.basic.model.ExceptionCode;
import tcsyn.basic.model.ResponseModel;
import tcsyn.basic.util.HttpClientUtil;
import tcsyn.basic.util.JsonUtil;

/**
 * 网关
 * @author shiwei
 * @date 2018年9月29日
 */
@Controller
@RequestMapping("gateway")
public class GatewayController extends AbstractController {
	
	@Autowired
	ScienerService scienerService;
	
	@Value("${sciener.appid}")
	String appid;
	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	OperationLogService operationLogService;
	
	@Autowired
	GatewayService gatewayService;
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	LockService lockService;
	
	@Autowired
	UserMessageService userMessageService;
	
	@Autowired
	LockAttchService lockAttchService;
	
	/**
	 * @apiDefine GatewayGroup 网关
	 */
	
	/**
	 * @api {GET} gateway/init/success 网关是否添加成功
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 使用SDK添加网关后调用该接口，查询网关是否添加成功
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {String} gatewayNetMac 网关MAC
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Boolean} data 是否初始化成功
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "表示失败或否",
		    "data": false
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
	@RequestMapping("init/success")
	@ResponseBody
	public ResponseModel initSuccess(String userId, String gatewayNetMac) {
		
		if (!StringUtils.isNoneBlank(userId, gatewayNetMac)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("gatewayNetMac", gatewayNetMac);
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "gateway/isInitSuccess", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errorcode = (Integer) resultMap.get("errcode");
			if (errorcode != 0) {
				logger.error("gateway init is success failed: " + result);
				return super.success(resultMap.get("errmsg").toString(), false);
			}
			return super.successData(true);
		} catch (Exception e) {
			throw new SystemException("查询网关是否初始化成功失败", e);
		}
	}
	
	/**
	 * @api {POST} gateway/add 添加网关
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 使用SDK添加网关后调用该接口绑定网关名称
	 * 
	 * @apiParam {String} gatewayMac 网关Mac
	 * @apiParam {String} name 网关名称
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
	@RequestMapping("add")
	@ResponseBody
	public ResponseModel add(String gatewayMac, String name) {
		if (!StringUtils.isNoneBlank(gatewayMac, name)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		this.redisTemplate.opsForValue().set("gateway." + gatewayMac, name);
		return super.success();
	}
	
	/**
	 * @api {GET} gateway/list 网关列表
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 查询用户下网关列表信息
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} [pageNo=1] 页码
	 * @apiParam {Integer} [pageSize=20] 每页数量
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Gateway[]} data 网关列表
	 * 
	 * @apiSuccess (Gateway) {Integer} gatewayId 网关ID
	 * @apiSuccess (Gateway) {String} gatewayMac 网关mac地址
	 * @apiSuccess (Gateway) {String} gatewayName 网关NET MAC地址
	 * @apiSuccess (Gateway) {String} name 网关名称
	 * @apiSuccess (Gateway) {Integer} lockNum 网关管理的锁数量
	 * @apiSuccess (Gateway) {Integer} isOnline 是否在线：0-否，1-是
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": [
		    	{
		            "gatewayId": 123156,
		            "gatewayMac": "52:A6:D8:B2:C1:00",
		            "gatewayName": "F0FE6BA150B6",
		            "lockNum": 5,
		            "isOnline": 1,
		            "name": "天眼"
		        }, {
		            "gatewayId": 123172,
		            "gatewayMac": "52:A6:D8:3D:C1:E3",
		            "gatewayName": "ABD650B600E1",
		            "lockNum": 2,
		            "isOnline": 1,
		            "name": "网关名称"
		        }
		    ]
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 科技侠接口返回错误
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("list")
	@ResponseBody
	public ResponseModel list(String userId, @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "20") Integer pageSize) {
		if (!StringUtils.isNoneBlank(userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("pageNo", pageNo.toString());
		paramMap.put("pageSize", pageSize.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "gateway/list", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			List<Map<String, Object>> gatewayList = (List<Map<String, Object>>) resultMap.get("list");
			if (gatewayList == null) {
				logger.error("Get gateway list failed: " + result);
				return super.fail(resultMap.get("errmsg").toString(), ExceptionCode.BX1);
			}
			ValueOperations<String, String> ops = this.redisTemplate.opsForValue();
			for (Map<String, Object> map : gatewayList) {
				String name = ops.get("gateway." + map.get("gatewayName"));
				map.put("name", name);
			}
			return super.successData(gatewayList);
		} catch (Exception e) {
			throw new SystemException("查询网关列表失败", e);
		}
	}
	
	/**
	 * @api {POST} gateway/delete 删除网关
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 根据网关id删除网关
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} gatewayId 网关id
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
	   
	   @apiErrorExample {json} 科技侠接口返回错误
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "No such Gateway exists.",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("delete")
	@ResponseBody
	public ResponseModel delete(Integer gatewayId, String userId) {
		if (!StringUtils.isNoneBlank(userId) || gatewayId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("gatewayId", gatewayId.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "gateway/delete", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errorcode = (Integer) resultMap.get("errcode");
			if (errorcode != 0) {
				logger.error("gateway to delete failed: " + result);
				return super.fail(resultMap.get("errmsg").toString(), ExceptionCode.BX1);
			}
			return super.success();
		} catch (Exception e) {
			throw new SystemException("删除网关失败", e);
		}
	}
	
	/**
	 * @api {GET} gateway/lock/list 网关锁列表
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 根据网关id查询网关下的锁列表信息
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} gatewayId 网关id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Lock[]} data 锁列表
	 * 
	 * @apiSuccess (Lock) {Integer} lockId 锁ID
	 * @apiSuccess (Lock) {String} lockMac 锁mac地址
	 * @apiSuccess (Lock) {String} lockName 锁名
	 * @apiSuccess (Lock) {Integer} rssi 网关与锁之间信号强度，参考标准：大于-75为强，大于-85小于-75为中，小于-85为弱。
	 * @apiSuccess (Lock) {Long} updateDate RSSI信号强度更新时间
	 * @apiSuccess (Lock) {String} alias 锁别名
	 * @apiSuccess (Lock) {Integer} specialValue 锁特征值
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": [
		        {
		            "lockId": 123156,
		            "lockMac": "52:A6:D8:B2:C1:00",
		            "lockName": "M202_54444",
		            "rssi": -80,
		            "updateDate": 1490247682268,
		            "alias": "温馨的家"
		        }
		    ]
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 科技侠接口返回错误
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "No such Gateway exists.",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("lock/list")
	@ResponseBody
	public ResponseModel lockList(Integer gatewayId, String userId) {
		if (!StringUtils.isNoneBlank(userId) || gatewayId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("gatewayId", gatewayId.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "gateway/listLock", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			List<Map<String, Object>> lockList = (List<Map<String, Object>>) resultMap.get("list");
			if (lockList == null) {
				logger.error("gateway get lock list failed: " + result);
				return super.fail(resultMap.get("errmsg").toString(), ExceptionCode.BX1);
			}
			for (Map<String, Object> map : lockList) {
				Integer lockId = (Integer) map.get("lockId");
				Lock lock = this.lockService.getLockById(lockId);
				map.put("alias", lock.getAlias());
				LockAttch lockAttch = this.lockAttchService.getLockAttchById(lockId);
				map.put("specialValue", lockAttch.getSpecialValue());
			}
			return super.successData(lockList);
		} catch (Exception e) {
			throw new SystemException("获取网关锁列表失败", e);
		}
	}
	
	/**
	 * @api {POST} gateway/unlock 网关开锁
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 利用网关远程打开锁id对应的锁
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 需要开锁的锁id
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
	   
	   @apiErrorExample {json} 科技侠接口返回错误
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "No such Gateway exists.",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("unlock")
	@ResponseBody
	public ResponseModel unlock(String userId, Integer lockId) {
		if (!StringUtils.isNoneBlank(userId) || lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("lockId", lockId.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/unlock", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errorcode = (Integer) resultMap.get("errcode");
			if (errorcode != 0) {
				logger.error("gateway unlock failed: " + result);
				return super.fail(resultMap.get("errmsg").toString(), ExceptionCode.BX1);
			}
			// 记录操作日志
			this.operationLogService.addLog(userId, lockId, "Unlock with gateway", 4, null);
			try {
				Lock lock = this.lockService.getLockById(lockId);
				if(null != lock) {
					userMessageService.addUserMessage(lock.getUserId(), AbstractController.getText("gateway.unlock.title"), String.format( AbstractController.getText("gateway.unlock.content"), lock.getAlias()), 11);
				}
			}catch (Exception e) {
				logger.error("gateway unlock send mssage failed");
			}
			return super.success();
		} catch (Exception e) {
			throw new SystemException("网关开锁失败", e);
		}
	}
	
	/**
	 * @api {POST} gateway/lock 网关闭锁
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 利用网关远程关闭锁id对应的锁
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 需要关闭的锁id
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
	   
	   @apiErrorExample {json} 科技侠接口返回错误
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "No such Gateway exists.",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("lock")
	@ResponseBody
	public ResponseModel lock(String userId, Integer lockId) {
		if (!StringUtils.isNoneBlank(userId) || lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("lockId", lockId.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/lock", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer errorcode = (Integer) resultMap.get("errcode");
			if (errorcode != 0) {
				logger.error("gateway unlock failed: " + result);
				return super.fail(resultMap.get("errmsg").toString(), ExceptionCode.BX1);
			}
			// 记录操作日志
			this.operationLogService.addLog(userId, lockId, "Lock with gateway", 5, null);
			try {
				Lock lock = this.lockService.getLockById(lockId);
				if(null != lock) {
					userMessageService.addUserMessage(lock.getUserId(), AbstractController.getText("gateway.lock.title"), String.format(AbstractController.getText("gateway.lock.content"), lock.getAlias()), 12);
				}
			}catch (Exception e) {
				logger.error("gateway unlock send mssage failed");
			}
			return super.success();
		} catch (Exception e) {
			throw new SystemException("网关闭锁失败", e);
		}
	}
	
	/**
	 * @api {POST} gateway/freeze 网关冻结锁
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 利用网关远程冻结锁，冻结后，所有开锁方式都禁用，但蓝牙还是可以连接的，以便冻结后还可以解冻。
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 需要开锁的锁id
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
	@RequestMapping("freeze")
	@ResponseBody
	public ResponseModel freeze(String userId, Integer lockId) {
		if (!StringUtils.isNoneBlank(userId) || lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		this.gatewayService.freeze(userId, lockId);
		return super.success();
	}
	
	/**
	 * @api {POST} gateway/unfreeze 网关解冻锁
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 利用网关远程网关解冻锁
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 需要开锁的锁id
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
	@RequestMapping("unfreeze")
	@ResponseBody
	public ResponseModel unfreeze(String userId, Integer lockId) {
		if (!StringUtils.isNoneBlank(userId) || lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		this.gatewayService.unfreeze(userId, lockId);
		return super.success();
	}
	
	/**
	 * @api {GET} gateway/is/freeze 查询锁是否冻结
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 查询锁是否冻结
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 需要开锁的锁id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Integer} data 锁的冻结状态：0-未冻结，1-冻结
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": 1
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 科技侠接口返回错误
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "No such Gateway exists.",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("is/freeze")
	@ResponseBody
	public ResponseModel isFreeze(String userId, Integer lockId) {
		if (!StringUtils.isNoneBlank(userId) || lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("lockId", lockId.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/queryStatus", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer status = (Integer) resultMap.get("status");
			if (status == null) {
				logger.error("gateway is freeze failed: " + result);
				return super.fail(resultMap.get("errmsg").toString(), ExceptionCode.BX1);
			}
			return super.successData(status);
		} catch (Exception e) {
			throw new SystemException("查询锁的冻结状态失败", e);
		}
	}
	
	/**
	 * @api {GET} gateway/userId/get 获取用户主键ID
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroup
	 * @apiDescription 通过用户id获取sciener用户主键id
	 * 
	 * @apiParam {String} userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Integer} data 用户主键id
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": 22556
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 科技侠接口返回错误
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "No such Gateway exists.",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("userId/get")
	@ResponseBody
	public ResponseModel getUserId(String userId) {
		if (!StringUtils.isNoneBlank(userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "user/getUid", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer uid = (Integer) resultMap.get("uid");
			if (uid == null) {
				logger.error("获取用户主键ID失败: " + result);
				return super.fail(resultMap.get("errmsg").toString(), ExceptionCode.BX1);
			}
			return super.successData(uid);
		} catch (Exception e) {
			throw new SystemException("获取用户主键ID失败", e);
		}
	}
	
	/**
	 * @api {GET} gateway/v2/lock/queryOpenState 查询锁开关状态
	 * @apiVersion 1.0.0
	 * @apiGroup GatewayGroupV2
	 * @apiDescription 查询锁开关状态
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 需要开锁的锁id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Integer} data 锁的开关状态:0-关,1-开,2-未知
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": 1
		}
	 * 
	 * @apiErrorExample {json} 必填参数为空
	 * {
		    "success": false,
		    "code": 900,
		    "msg": "Some parameters are required",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 科技侠接口返回错误
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "No such Gateway exists.",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("lock/queryOpenState")
	@ResponseBody
	public ResponseModel queryOpenState(String userId, Integer lockId) {
		if (!StringUtils.isNoneBlank(userId) || lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("clientId", this.appid);
		paramMap.put("accessToken", this.scienerService.getAccessToken(userId));
		paramMap.put("lockId", lockId.toString());
		paramMap.put("date", String.valueOf(System.currentTimeMillis()));
		
		try {
			String result = HttpClientUtil.httpPost(Constants.SCIENER_OPEN_API_PREFIX + "lock/queryOpenState", paramMap);
			Map<String, Object> resultMap = JsonUtil.fromJson(result, new TypeReference<Map<String, Object>>() {});
			Integer state = (Integer) resultMap.get("state");
			if (state == null) {
				logger.error("gateway is queryOpenState failed: " + result);
				return super.fail(resultMap.get("errmsg").toString(), ExceptionCode.BX1);
			}
			return super.successData(state);
		} catch (Exception e) {
			throw new SystemException("查询锁的开关状态失败", e);
		}
	}
	
}
