/*
 * Copyright (c) 2017-2019, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.aop;

import java.lang.reflect.Method;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.igloo.smarthome.constant.UserLogAnno;
import com.igloo.smarthome.service.basic.UserLogService;

import tcsyn.basic.model.ResponseModel;
import tcsyn.basic.util.JsonUtil;

/**
 * 
 * @author Ares S
 * @date 2020年5月25日
 */
@Aspect
@Component
public class UserLogAspect {
	
	@Pointcut("@annotation(com.igloo.smarthome.constant.UserLogAnno)")
	private void anyMethod() {}
	
	Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	UserLogService userLogService;
	
	@Around("anyMethod()")
	public Object execute(ProceedingJoinPoint pjp) throws Throwable {
		Date currentDate = new Date();
		String title = null;
		String userIdKey = null;
		try {
			Signature s = pjp.getSignature();
			MethodSignature ms = (MethodSignature)s;
			Method method = ms.getMethod();
			UserLogAnno userLogAnno = method.getAnnotation(UserLogAnno.class);
			title = userLogAnno.title();
			userIdKey = userLogAnno.userIdKey();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		Object resultObj = pjp.proceed();
		try {
			Boolean processSuccessed = false;
			if (resultObj instanceof ResponseModel) {
				ResponseModel rm = (ResponseModel) resultObj;
				if (rm.isSuccess()) {
					processSuccessed = true;
				}
			}
			RequestAttributes ra = RequestContextHolder.getRequestAttributes();
			ServletRequestAttributes sra = (ServletRequestAttributes) ra;
			HttpServletRequest request = sra.getRequest();
			String paramValues = JsonUtil.toJson(request.getParameterMap());
			this.userLogService.addUserLog(currentDate, paramValues, request.getParameter(userIdKey), processSuccessed, title, JsonUtil.toJson(resultObj));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return resultObj;
	}
}
