/*
 * Copyright (c) 2018, szmuen and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.igloo.smarthome.constant;

/**
 * 常量
 * @author shiwei
 * @date 2018年8月19日
 */
public final class Constants {
	
	public static final String SCIENER_PREFIX = "https://api.sciener.cn/";

	/** 科技侠开放接口前缀 */
	public static final String SCIENER_OPEN_API_PREFIX = SCIENER_PREFIX + "v3/";
	
	public static final String SCIENER_TOKEN_PREFIX = SCIENER_PREFIX + "oauth2/token";
	
	/** 通用失效标志 */
	public static final String SIGN_OF = "OF"; 
	
	public static final String YES = "Y";

	public static final String NO = "N";
	
	public static class System {
		
		public static final String IOS = "IOS";
		
		public static final String ANDROID = "Android";
		
		public static final String PC = "PC";
	}
	/**
	 * 钥匙状态
	 * @author lijq
	 * @Date 2018年8月22日
	 */
	public static class KeyStatus{
		/** 正常使用 */
		public static final String NORMAL = "110401";
		
		/** 待接收 */
		public static final String WATINGRECEIVE = "110402";
		
		/** 已冻结 */
		public static final String FROZEN = "110405";
		
		/** 已删除 */
		public static final String DELETED = "110408";
		
		/** 已重置 */
		public static final String RESET = "110410";
		
		/** 已过期 */
		public static final String EXPIRE = "110500";
	}
	
	/**
	 * 钥匙用户类型
	 * @author lijq
	 * @Date 2018年8月22日
	 */
	public static class KeyUserType{
		
		/** 管理员钥匙 */
		public static final String MANGER = "110301";
		
		/** 普通用户钥匙 */
		public static final String USER = "110302";
	}
	
	/**
	 * 钥匙删除类型
	 * @author lijq
	 * @Date 2018年9月13日
	 */
	public static class KeyDeleteType{
		
		/** 正常（没有删除） */
		public static final Integer NORMAL = 0;
		
		/** 通过蓝牙删除 */
		public static final Integer BLUETOOTH = 1;
		
		/** 通过网关删除 */
		public static final Integer ZUUL = 2;
	}
	/**
	 * 键盘密码删除类型
	 * @author lijq
	 * @Date 2018年8月27日
	 */
	public static class KeyboardPwdDeleteType{
		
		/** 正常（没有删除） */
		public static final Integer NORMAL = 0;
		
		/** 通过蓝牙删除 */
		public static final Integer BLUETOOTH = 1;
		
		/** 通过网关删除 */
		public static final Integer ZUUL = 2;
	}
	
	/**
	 * 键盘密码状态
	 * @author lijq
	 * @Date 2018年8月27日
	 */
	public static class KeyboardPwdStatus{
		
		/** 删除 */
		public static final Integer DELETED = 0;
		
		/** 未激活 */
		public static final Integer UNACTIVATION = 1;
		
		/** 过期失效 */
		public static final Integer INVALID = 2;
		
		/** 正常 */
		public static final Integer NORMAL = 3;
		
		/** 未知 */
		public static final Integer UNKNOWN = 4;
		
	}
	
	/**
	 * 键盘密码类型
	 * @author lijq
	 * @Date 2018年9月11日
	 */
	public static class KeyboardPwdType{
		
		/** 单次 */
		public static final Integer ONE = 1;
		
		/** 永久 */
		public static final Integer PERMANENT = 2;
		
		/** 限期 */
		public static final Integer DEADLINE = 3;
		
		/** 删除 */
		public static final Integer DELETED = 4;
		
		/** 周未循环 */
		public static final Integer WEEKEND_LOOP = 5;
		
		/** 每日循环 */
		public static final Integer DAY_LOOP = 6;
		
		/** 工作日循环 */
		public static final Integer WEEKDAY_LOOP = 7;
		
		/** 周一循环 */
		public static final Integer MONDAY_LOOP = 8;
		
		/** 周二循环 */
		public static final Integer TUESDAY_LOOP = 9;
		
		/** 周三循环*/
		public static final Integer WEDNESDAY_LOOP = 10;
		
		/** 周四循环 */
		public static final Integer THURSDAY_LOOP = 11;
		
		/** 周五循环 */
		public static final Integer FRIDAY_LOOP = 12;
		
		/** 周六循环	 */
		public static final Integer SATURDAY_LOOP = 13;
		
		/** 周天循环 */
		public static final Integer SUNDAY_LOOP = 14;
		
	}
	
	/**
	 * redis
	 * @author lijq
	 * @Date 2018年8月24日
	 */
	public static class RedisKey{
		
		/** 最近同步钥匙时间 */
		public static final String LASTUPDATEDATE = "lastUpdateDate";
		
	}
	
	/**
	 * 锁状态
	 * @author lijq
	 * @Date 2018年9月6日
	 */
	public static class LockStatus{
		
		public static final Integer DELETED = 0;
		
		public static final Integer NORMAL = 1;
		
		public static final Integer FREEZE = 2;
		
	}
	/**
	 * 钥匙是否被授权
	 * @author lijq
	 * @Date 2018年9月6日
	 */
	public static class KeyRight{
		
		public static final Integer NO = 0;
		
		public static final Integer YES = 1;
		
	}
	
	/**
	 * 钥匙类型
	 * @author lijq
	 * @Date 2018年9月6日
	 */
	public static class KeyType{
		
		/** 限时 */
		public static final Integer LIMITTIME = 1;
		
		/** 永久*/
		public static final Integer FOREVER = 2;
		
		/** 单次 */
		public static final Integer ONE = 3;
		
	}
	
	/**
	 *  通讯介质类型
	 * @author ljq
	 * @Date 2020年5月13日
	 * @company 深沐恩
	 */
	public static class MediumType{
		
		/** 通过蓝牙*/
		public static final Integer BLUETOOTH = 1;
		
		/** 通过网关 */
		public static final Integer ZUUL = 2;
		
		/** 通过NB-IoT */
		public static final Integer NBIOT = 3;
	}
}
