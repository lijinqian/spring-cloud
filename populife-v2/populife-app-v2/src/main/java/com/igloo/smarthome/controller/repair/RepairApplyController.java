/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.controller.repair;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.igloo.smarthome.constant.RepairStatusEnum;
import com.igloo.smarthome.controller.AbstractAppController;
import com.igloo.smarthome.model.Lock;
import com.igloo.smarthome.model.repair.RepairApply;
import com.igloo.smarthome.model.repair.RepairApplyApprove;
import com.igloo.smarthome.model.repair.RepairApplyApproveFail;
import com.igloo.smarthome.model.repair.RepairApplyLock;
import com.igloo.smarthome.model.repair.RepairConclusion;
import com.igloo.smarthome.model.repair.RepairConsignee;
import com.igloo.smarthome.model.repair.RepairConsumablesManifest;
import com.igloo.smarthome.model.repair.RepairLocations;
import com.igloo.smarthome.model.repair.RepairProgress;
import com.igloo.smarthome.service.LockService;
import com.igloo.smarthome.service.repair.RepairApplyApproveService;
import com.igloo.smarthome.service.repair.RepairApplyLockService;
import com.igloo.smarthome.service.repair.RepairApplyService;
import com.igloo.smarthome.service.repair.RepairConclusionService;
import com.igloo.smarthome.service.repair.RepairConsigneeService;
import com.igloo.smarthome.service.repair.RepairConsumablesManifestService;
import com.igloo.smarthome.service.repair.RepairLocationsService;
import com.igloo.smarthome.service.repair.RepairProgressService;

import tcsyn.basic.model.ExceptionCode;
import tcsyn.basic.model.ResponseModel;

/**
 * 
 * @author Ares S
 * @date 2020年6月8日
 */
@Controller
@RequestMapping("repair-apply")
public class RepairApplyController extends AbstractAppController {
	
	@Autowired
	RepairLocationsService repairLocationsService;
	
	@Autowired
	RepairApplyService repairApplyService;
	
	@Autowired
	RepairApplyLockService repairApplyLockService;
	
	@Autowired
	LockService lockService;
	
	@Autowired
	RepairConsigneeService repairConsigneeService;
	
	@Autowired
	RepairApplyApproveService repairApplyApproveService;
	
	@Autowired
	RepairProgressService repairProgressService;
	
	@Autowired
	RepairConclusionService repairConclusionService;
	
	@Autowired
	RepairConsumablesManifestService repairConsumablesManifestService;
	
	/**
	 * @apiDefine RepairGroup 维修服务
	 */
	
	/**
	 * @api {GET} repair-apply/repair-locations/get 获取维修点信息
	 * @apiVersion 1.0.0
	 * @apiGroup RepairGroup
	 * @apiDescription 获取维修点信息
	 * 
	 * @apiParam {String} countryCode 国家二字码
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data 维修点信息
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": {
		    	"id": "zzhfkpxx8z",
		        "name": "波普生活纽约售后服务中心",
		        "location": "纽约市M区F街道72号",
		        "receiptAddress": "美国 纽约州 纽约市 M区F街道72号 公司名称：波普生活   电话：+16525458526",
		        "telephone": "+16525458526",
		        "remark": "请勿使用平邮和到付，无需寄回电池",
		        "countryCode": "US",
		        "postcode": "7252054"
		    }
		}
	 * 
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("repair-locations/get")
	@ResponseBody
	public ResponseModel getRepairLocations(String countryCode) {
		if (StringUtils.isBlank(countryCode)) {
			return super.fail("CountryCode has required", ExceptionCode.ARG_EMPTY);
		}
		RepairLocations rl = this.repairLocationsService.getRepairLocations(countryCode);
		return super.successData(rl);
	}
	
	/**
	 * @api {POST} repair-apply/submit 提交维修申请
	 * @apiVersion 1.0.0
	 * @apiGroup RepairGroup
	 * @apiDescription 提交维修申请
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {String} description 问题描述
	 * @apiParam {String} modelNum 型号
	 * @apiParam {String} purchasedDate 购买日期
	 * @apiParam {File} purchasedTicketImg 购买凭证电子图片
	 * @apiParam {String} countryCode 国家二字码
	 * @apiParam {String} name 姓名
	 * @apiParam {String} emailAddress 邮箱地址
	 * @apiParam {String} telephone 电话
	 * @apiParam {String} province 省份
	 * @apiParam {String} city 城市
	 * @apiParam {String} streetAddress 街道门牌号
	 * @apiParam {String} postcode 邮编
	 * @apiParam {Integer} [lockId] 锁id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Submit successed",
    		"data": null
		}
	 * 
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("submit")
	@ResponseBody
	public ResponseModel submitRepairApply(Integer lockId, RepairApply repairApply, RepairConsignee repairConsignee, MultipartFile purchasedTicketImg) {
		if (repairApply == null || repairApply.isEmpty() || repairConsignee == null || repairConsignee.isEmpty() || purchasedTicketImg == null) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		if (lockId != null) {
			RepairApply uncompleteRepairApply = this.repairApplyService.getUncompleteByLockId(lockId);
			if (uncompleteRepairApply != null) {
				return super.fail("Lock has been applied for repair", ExceptionCode.BX1);
			}
		}
		String applyNo = Long.toString(System.nanoTime(), 14).toUpperCase();
		String purchasedTicketImgUrl = super.uploadFile(purchasedTicketImg, "repair");
		repairApply.setPurchasedTicket(purchasedTicketImgUrl);
		repairApply.setApplyNo(applyNo);
		repairApply.setCancelled(false);
		repairApply.setCreateDate(new Date());
		repairApply.setStatus(RepairStatusEnum.submitted.code);
		repairConsignee.setApplyNo(applyNo);
		this.repairApplyService.addRepairApply(repairApply, repairConsignee, new RepairApplyLock(applyNo, lockId));
		return super.success("Submit successed");
	}
	
	/**
	 * @api {GET} repair-apply/get 获取用户的维修申请列表
	 * @apiVersion 1.0.0
	 * @apiGroup RepairGroup
	 * @apiDescription 获取用户的维修申请列表
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {String} [start=0] 起始记录数
	 * @apiParam {String} [limit=10] 记录行数
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data 维修申请信息
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": [
		        {
		            "applyNo": "66890AC342DCB",
		            "userId": "26k8xsoi9sbi",
		            "description": "不能添加锁，当我开启手机蓝牙，锁安装好池之后，蓝牙无法扫描到锁",
		            "modelNum": "PPL_SN103998",
		            "purchasedTicket": "http://localhost/files/repair/104rs5jekp4.jpg",
		            "purchasedDate": 1589644800000,
		            "createDate": 1592126963000,
		            "cancelled": false,
		            "status": 1,
		            "lockName": null,
		            "empty": false
		        }
		    ]
		}
	 * 
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("get")
	@ResponseBody
	public ResponseModel getRepairApply(String userId, @RequestParam(defaultValue = "0") Integer start, @RequestParam(defaultValue = "10") Integer limit) {
		if (StringUtils.isBlank(userId)) {
			return super.fail("userId has required", ExceptionCode.ARG_EMPTY);
		}
		List<RepairApply> list = this.repairApplyService.getRepairApply(userId, start, limit);
		return super.successData(list);
	}
	
	/**
	 * @api {GET} repair-apply/detail/get 获取维修单详情
	 * @apiVersion 1.0.0
	 * @apiGroup RepairGroup
	 * @apiDescription 获取维修单详情
	 * 
	 * @apiParam {String} applyNo 申请单号
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data 维修单详情
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": {
		        "repairApply": {
		            "applyNo": "66890AC342DCB",
		            "userId": "26k8xsoi9sbi",
		            "description": "不能添加锁，当我开启手机蓝牙，锁安装好池之后，蓝牙无法扫描到锁",
		            "modelNum": "PPL_SN103998",
		            "purchasedTicket": "http://localhost/files/repair/104rs5jekp4.jpg",
		            "purchasedDate": 1589644800000,
		            "createDate": 1592126963000,
		            "cancelled": false,
		            "status": 1,
		            "lockName": null,
		            "empty": false
		        },
		        "repairProgressList": [
		            {
		                "applyNo": "66890AC342DCB",
		                "status": 1,
		                "remark": "--",
		                "createDate": 1592126963000
		            }
		        ],
		        "lock": null,
		        "repairConsignee": {
		            "applyNo": "66890AC342DCB",
		            "countryCode": "US",
		            "name": "Ares Jack",
		            "emailAddress": "49871320@qq.com",
		            "telephone": "+16554112552",
		            "province": "New York",
		            "city": "New York",
		            "streetAddress": "U区E街道32号",
		            "postcode": "856542",
		            "empty": false
		        }
		    }
		}
	 * 
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("detail/get")
	@ResponseBody
	public ResponseModel getRepairApplyDetail(String applyNo) {
		if (StringUtils.isBlank(applyNo)) {
			return super.fail("applyNo has required", ExceptionCode.ARG_EMPTY);
		}
		Map<String, Object> resultMap = new HashMap<>();
		RepairApply repairApply = this.repairApplyService.getById(applyNo);
		resultMap.put("repairApply", repairApply);
		RepairApplyLock repairApplyLock = this.repairApplyLockService.getById(applyNo);
		if (repairApplyLock != null) {
			Lock lock = this.lockService.getLockById(repairApplyLock.getLockId());
			resultMap.put("lock", lock);
		}
		RepairConsignee repairConsignee = this.repairConsigneeService.getById(applyNo);
		resultMap.put("repairConsignee", repairConsignee);
		if (RepairStatusEnum.isApproved(repairApply.getStatus())) {
			RepairApplyApprove repairApplyApprove = this.repairApplyApproveService.getById(applyNo);
			resultMap.put("repairApplyApprove", repairApplyApprove);
			if (!repairApplyApprove.getApproved()) {
				RepairApplyApproveFail repairApplyApproveFail = this.repairApplyApproveService.getRepairApplyApproveFailById(applyNo);
				resultMap.put("repairApplyApproveFail", repairApplyApproveFail);
			}
		}
		List<RepairProgress> repairProgressList = this.repairProgressService.getRepairProgress(applyNo);
		resultMap.put("repairProgressList", repairProgressList);
		if (RepairStatusEnum.isDetected(repairApply.getStatus())) {
			RepairConclusion repairConclusion = this.repairConclusionService.getById(applyNo);
			resultMap.put("repairConclusion", repairConclusion);
			List<RepairConsumablesManifest> consumablesManifestList = this.repairConsumablesManifestService.getByApplyNo(applyNo);
			resultMap.put("consumablesManifestList", consumablesManifestList);
		}
		return super.successData(resultMap);
	}
	
	/**
	 * @api {POST} repair-apply/conclusion/agree 同意检测结果并维修
	 * @apiVersion 1.0.0
	 * @apiGroup RepairGroup
	 * @apiDescription 申请单状态为已检测时，用户可以同意检测结果并进入维修
	 * 
	 * @apiParam {String} applyNo 申请单号
	 * @apiParam {String} [remark] 备注
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
	@RequestMapping("conclusion/agree")
	@ResponseBody
	public ResponseModel agreeConclusion(String applyNo, String remark) {
		if (StringUtils.isBlank(applyNo)) {
			return super.fail("applyNo has required", ExceptionCode.ARG_EMPTY);
		}
		RepairApply repairApply = this.repairApplyService.getById(applyNo);
		if (!repairApply.getStatus().equals(RepairStatusEnum.detected.code)) {
			return super.fail("Invalid status of repair", ExceptionCode.ARG_INVALID);
		}
		this.repairApplyService.updateStatus(applyNo, RepairStatusEnum.user_agree.code, remark);
		return super.success();
	}
	
	/**
	 * @api {POST} repair-apply/finish 结束维修单
	 * @apiVersion 1.0.0
	 * @apiGroup RepairGroup
	 * @apiDescription 用户收到返修产品后，确认产品已经修理好，点击结束维修单
	 * 
	 * @apiParam {String} applyNo 申请单号
	 * @apiParam {String} [remark] 备注
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
	@RequestMapping("finish")
	@ResponseBody
	public ResponseModel finish(String applyNo, String remark) {
		if (StringUtils.isBlank(applyNo)) {
			return super.fail("applyNo has required", ExceptionCode.ARG_EMPTY);
		}
		RepairApply repairApply = this.repairApplyService.getById(applyNo);
		if (!repairApply.getStatus().equals(RepairStatusEnum.sent_back.code) && !repairApply.getStatus().equals(RepairStatusEnum.delivered.code)) {
			return super.fail("Invalid status of repair", ExceptionCode.ARG_INVALID);
		}
		this.repairApplyService.updateStatus(applyNo, RepairStatusEnum.completed.code, remark);
		return super.success();
	}
	
	/**
	 * @api {POST} repair-apply/cancel 取消维修单
	 * @apiVersion 1.0.0
	 * @apiGroup RepairGroup
	 * @apiDescription 申请单状态在用户同意之前都可取消维修单
	 * 
	 * @apiParam {String} applyNo 申请单号
	 * @apiParam {String} remark 备注
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
	@RequestMapping("cancel")
	@ResponseBody
	public ResponseModel cancel(String applyNo, String remark) {
		if (StringUtils.isBlank(applyNo)) {
			return super.fail("applyNo has required", ExceptionCode.ARG_EMPTY);
		}
		if (StringUtils.isBlank(remark)) {
			return super.fail("remark has required", ExceptionCode.ARG_EMPTY);
		}
		RepairApply repairApply = this.repairApplyService.getById(applyNo);
		if (repairApply.getStatus() > RepairStatusEnum.user_agree.code) {
			return super.fail("Invalid status of repair", ExceptionCode.ARG_INVALID);
		}
		this.repairApplyService.cancelRepairApply(applyNo, remark);
		return super.success();
	}
}
