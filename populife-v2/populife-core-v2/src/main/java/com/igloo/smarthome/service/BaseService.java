package com.igloo.smarthome.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.igloo.smarthome.ext.ScienerService;

@Service
public class BaseService {
	
	@Value("${sciener.appid}")
	String appid;
	
	@Value("${sciener.appsecret}")
	String appsecret;
	
	@Autowired
	ScienerService scienerService;
	
}
