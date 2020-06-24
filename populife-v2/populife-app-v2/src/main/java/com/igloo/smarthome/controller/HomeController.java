/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.controller;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.igloo.smarthome.model.Home;
import com.igloo.smarthome.service.HomeService;

import tcsyn.basic.controller.AbstractController;
import tcsyn.basic.model.ExceptionCode;
import tcsyn.basic.model.ResponseModel;
import tcsyn.basic.util.TextUtil;

/**
 * 家庭（分组）
 * @author shiwei
 * @date 2018年8月23日
 */
@Controller
@RequestMapping("home")
public class HomeController extends AbstractController {
	
	/**
	 * @apiDefine HomeGroup 家庭（分组）
	 */
	
	@Autowired
	HomeService homeService;
	
	/**
	 * @api {POST} home/add 添加家庭（分组）
	 * @apiVersion 1.0.0
	 * @apiGroup HomeGroup
	 * @apiDescription 添加家庭（分组），方便对锁分类管理
	 * 
	 * @apiParam {String} userId 用户id
	 * @apiParam {String} name 家庭（分组）名称
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Object} data null
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": "Successfully added",
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
	   
	   @apiErrorExample {json} 名称已经存在
	 * {
		    "success": false,
		    "code": 910,
		    "msg": "The name already exists",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("add")
	@ResponseBody
	public ResponseModel add(Home home) {
		if (!StringUtils.isNoneBlank(home.getUserId(), home.getName())) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Home bean = this.homeService.getByName(home);
		if (bean != null) {
			return super.fail("The name already exists", ExceptionCode.ARG_INVALID);
		}
		home.setCreateDate(new Date());
		home.setId(TextUtil.generateId());
		this.homeService.addHome(home);
		return super.success("Successfully added");
	}
	
	/**
	 * @api {POST} home/modify 修改家庭（分组）
	 * @apiVersion 1.0.0
	 * @apiGroup HomeGroup
	 * @apiDescription 修改家庭（分组）信息
	 * 
	 * @apiParam {String} id 家庭（分组）id
	 * @apiParam {String} userId 用户id
	 * @apiParam {String} name 家庭（分组）名称
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
	   
	   @apiErrorExample {json} 名称已经存在
	 * {
		    "success": false,
		    "code": 910,
		    "msg": "The name already exists",
		    "data": null
	   }
	   
	 * @apiUse ErrorExample
	 *	
	 */
	@RequestMapping("modify")
	@ResponseBody
	public ResponseModel modify(Home home) {
		if (!StringUtils.isNoneBlank(home.getUserId(), home.getName(), home.getId())) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		Home bean = this.homeService.getByName(home);
		if (bean != null) {
			return super.fail("The name already exists", ExceptionCode.ARG_INVALID);
		}
		this.homeService.updateHome(home);
		return super.success("Successfully updated");
	}
	
	
	/**
	 * @api {POST} home/delete 删除家庭（分组）
	 * @apiVersion 1.0.0
	 * @apiGroup HomeGroup
	 * @apiDescription 删除家庭（分组）信息，交将关联的锁改为未关联家庭（未分组）
	 * 
	 * @apiParam {String} id 家庭（分组）id
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
	public ResponseModel delete(String id) {
		if (!StringUtils.isNoneBlank(id)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		this.homeService.deleteHome(id);
		return super.success("Successfully deleted");
	}
	
	/**
	 * @api {GET} home/get 获取家庭（分组）信息
	 * @apiVersion 1.0.0
	 * @apiGroup HomeGroup
	 * @apiDescription 获取家庭（分组）信息和分组下锁的数量
	 * 
	 * @apiParam {String} userId 用户id
	 * 
	 * @apiUse SuccessParam
	 * @apiSuccess {Home[]} data 家庭（分组）信息列表
	 * 
	 * @apiSuccess (Home) {String} id 家庭（分组）id，为空时表示未分组
	 * @apiSuccess (Home) {String} name 名称，为空时表示未分组
	 * @apiSuccess (Home) {Long} createDate 创建时间（毫秒时间戳）
	 * @apiSuccess (Home) {Integer} lockCount 家庭（分组）下的锁数量
	 * 
	 * @apiSuccessExample {json} 请求成功返回样例:
	 * {
		    "success": true,
		    "code": 200,
		    "msg": null,
		    "data": [
		        {
		            "id": "dn669bqffm",
		            "name": "北京的家",
		            "createDate": 1535167127000,
		            "lockCount": 0
		        },
		        {
		            "id": "dn6dn3qnsm",
		            "name": "上海的家",
		            "createDate": 1535167143000,
		            "lockCount": 0
		        },
		        {
		            "id": "dn6luvzibb",
		            "name": "广州的家",
		            "createDate": 1535167161000,
		            "lockCount": 0
		        },
		        {
		            "id": null,
		            "name": null,
		            "createDate": null,
		            "lockCount": 18
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
	public ResponseModel get(String userId) {
		if (!StringUtils.isNoneBlank(userId)) {
			return super.fail("Some parameters are required", ExceptionCode.ARG_EMPTY);
		}
		List<Home> homeList = this.homeService.getHome(userId);
		return super.successData(homeList);
	}
}
