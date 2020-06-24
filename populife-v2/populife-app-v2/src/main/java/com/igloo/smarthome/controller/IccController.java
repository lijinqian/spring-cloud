/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.igloo.smarthome.model.Icc;
import com.igloo.smarthome.service.IccService;

import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.model.ExceptionCode;
import tcsyn.basic.model.ResponseModel;
import tcsyn.basic.util.DateUtil;
import tcsyn.basic.util.JsonUtil;

/**
 * IC卡
 * @author shiwei
 * @date 2018年9月11日
 */
@Controller
@RequestMapping("icc")
public class IccController extends AbstractController {
	
	/**
	 * @apiDefine IccGroup IC卡
	 */
	@Autowired
	IccService iccService;
	
	/**
	 * @api {POST} icc/add 添加IC卡
	 * @apiVersion 1.0.0
	 * @apiGroup IccGroup
	 * @apiDescription 通过蓝牙在锁上添加IC卡后调用该接口。
	 * 
	 * @apiParam {String} cardNumber 卡号
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {String} [startDate] 有效期开始时间，示例值：2018-09-11 19:31
	 * @apiParam {String} [endDate] 有效期结束时间，示例值：2018-09-13 12:11
	 * @apiParam {String} [remark] 备注
	 * @apiParam {Integer} [timeZone] 时区
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
	   
	   @apiErrorExample {json} 重复添加
	 * {
		    "success": false,
		    "code": 951,
		    "msg": "Card number has been added",
		    "data": null
	   }
	   
	   @apiErrorExample {json} 日期格式错误
	 * {
		    "success": false,
		    "code": 952,
		    "msg": "Date `startDate` or `endDate` format was wrong",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("add")
	@ResponseBody
	public ResponseModel add(String cardNumber, Integer lockId, String startDate, String endDate, String remark, Integer timeZone) {
		if (!StringUtils.isNoneBlank(cardNumber) || lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		
		final String dateFormat = "yyyy-MM-dd HH:mm";
		Icc icc = new Icc();
		icc.setCardNumber(cardNumber);
		icc.setCreateDate(new Date());
		icc.setLockId(lockId);
		icc.setRemark(remark);
		icc.setType(1);
		try {
			if (StringUtils.isNotBlank(startDate)) {
				icc.setStartDate(DateUtil.getDateTime(startDate, dateFormat, timeZone));
			}
			if (StringUtils.isNotBlank(endDate)) {
				icc.setEndDate(DateUtil.getDateTime(endDate, dateFormat, timeZone));
				icc.setType(2);
			}
		} catch (Exception e) {
			return super.fail("Date `startDate` or `endDate` format was wrong", ExceptionCode.BX2);
		}
		this.iccService.addIcc(icc, 1);
		return super.success(); 
	}
	
	/**
	 * @api {POST} icc/internal/upload 上传锁内ic卡
	 * @apiVersion 1.0.0
	 * @apiGroup IccGroup
	 * @apiDescription 上传锁内ic卡，如果records参数为空则删除该锁下所有的ic卡
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {String} records 记录数据(json字符串)
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
	@RequestMapping("internal/upload")
	@ResponseBody
	public ResponseModel uploadInternal(String userId, Integer lockId, String records) {
		if (StringUtils.isBlank(userId) || lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		if (StringUtils.isBlank(records)) {
			this.iccService.deleteAllIcc(lockId, false);
			return super.success();
		}
		List<Map<String, Object>> recordList = JsonUtil.fromJson(records, new TypeReference<List<Map<String, Object>>>() {});
		List<Icc> iccList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(recordList)) {
			for (Map<String, Object> recordMap : recordList) {
				String cardNumber = (String) recordMap.get("cardNumber");
				Icc icc = this.iccService.getByCardNumber(cardNumber, lockId);
				if (icc == null) {
					icc = new Icc();
					icc.setCardNumber(cardNumber);
					icc.setCreateDate(new Date());
					icc.setType(1);
					icc.setLockId(lockId);
					icc.setRemark(cardNumber);
					iccList.add(icc);
				}
			}
			this.iccService.addIcc(iccList, userId, lockId);
		} else {
			this.iccService.deleteAllIcc(lockId, false);
		}
		return super.success();
	} 
	
	/**
	 * @api {POST} icc/delete 删除IC卡
	 * @apiVersion 1.0.0
	 * @apiGroup IccGroup
	 * @apiDescription 通过卡号删除IC卡
	 * 
	 * @apiParam {String} cardNumber 卡号
	 * @apiParam {Integer} lockId 锁id
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
	@RequestMapping("delete")
	@ResponseBody
	public ResponseModel delete(String cardNumber, Integer lockId) {
		if (!StringUtils.isNoneBlank(cardNumber) || lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		this.iccService.deleteIcc(cardNumber, 1, lockId);
		return super.success();
	}
	
	/**
	 * @api {POST} icc/empty 清空IC卡
	 * @apiVersion 1.0.0
	 * @apiGroup IccGroup
	 * @apiDescription 删除锁id下所有的IC卡
	 * 
	 * @apiParam {Integer} lockId 锁id
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
	@RequestMapping("empty")
	@ResponseBody
	public ResponseModel empty(Integer lockId) {
		if (lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		this.iccService.deleteAllIcc(lockId, true);
		return super.success();
	}
	
	/**
	 * @api {GET} icc/get 获取IC卡
	 * @apiVersion 1.0.0
	 * @apiGroup IccGroup
	 * @apiDescription 分页获取或按关键词搜索（卡号或备注）锁id下所有的IC卡
	 * 
	 * @apiParam {Integer} lockId 锁id
	 * @apiParam {Integer} [start=0] 页码
	 * @apiParam {Integer} [limit=20] 记录数
	 * @apiParam {String} keyword 关键词
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Icc[]} data ic卡列表信息
	 * 
	 * @apiSuccess (Icc) {Integer} lockId 锁id
	 * @apiSuccess (Icc) {String} cardNumber ic卡号
	 * @apiSuccess (Icc) {String} remark 备注
	 * @apiSuccess (Icc) {Long} startDate 有效期开始时间
	 * @apiSuccess (Icc) {Long} endDate 有效期结束时间
	 * @apiSuccess (Icc) {Integer} cardId ic卡ID
	 * @apiSuccess (Icc) {Long} createDate 创建时间
	 * @apiSuccess (Icc) {Integer} type 类型，1：永久，2：限时
	 * @apiSuccess (Icc) {String} expire 过期状态，Y过期， N未过期
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": [
		    	{
		    		"lockId": 1264371,
		    		"cardNumber": "1000023",
		    		"remark": "A",
		    		"startDate": 1558363521101,
		    		"endDate": 155834323234,
		    		"cardId": 15223,
		    		"createDate": 155834323234,
		    		"type": 1,
		    		"expire":"Y"
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
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("get")
	@ResponseBody
	public ResponseModel get(Integer lockId, @RequestParam(defaultValue = "0") Integer start, @RequestParam(defaultValue = "20") Integer limit, String keyword) {
		if (lockId == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		List<Icc> iccList = this.iccService.getIcc(lockId, start, limit, keyword);
		return super.successData(iccList);
	}
}
