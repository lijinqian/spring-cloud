package com.igloo.smarthome.model.vo;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.igloo.smarthome.constant.Constants;

import tcsyn.basic.util.DateUtil;

public class KeyboardPwdVo {
	
	private Integer keyboardPwdId;
	private String keyboardPwd;
	private Integer keyboardPwdType;
	private Long startDate;
	private Long endDate;
	private Date createDate;
	private String alias;
	private String sendUser;
	
	@JsonIgnore
	private Date firstTime;
	
	@JsonIgnore
	private Integer deleteType;
	
	
	
	/** 0删除，1未激活，2过期失效， 3正常，4未知 */
	private Integer status;
	public Integer getKeyboardPwdId() {
		return keyboardPwdId;
	}
	public void setKeyboardPwdId(Integer keyboardPwdId) {
		this.keyboardPwdId = keyboardPwdId;
	}
	public String getKeyboardPwd() {
		return keyboardPwd;
	}
	public void setKeyboardPwd(String keyboardPwd) {
		this.keyboardPwd = keyboardPwd;
	}
	public Integer getKeyboardPwdType() {
		return keyboardPwdType;
	}
	public void setKeyboardPwdType(Integer keyboardPwdType) {
		this.keyboardPwdType = keyboardPwdType;
	}
	
	public Long getStartDate() {
		return startDate;
	}
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}
	public Long getEndDate() {
		return endDate;
	}
	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getFirstTime() {
		return firstTime;
	}
	public void setFirstTime(Date firstTime) {
		this.firstTime = firstTime;
	}
	
	public Integer getDeleteType() {
		return deleteType;
	}
	public void setDeleteType(Integer deleteType) {
		this.deleteType = deleteType;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public String getSendUser() {
		return sendUser;
	}
	public void setSendUser(String sendUser) {
		this.sendUser = sendUser;
	}
	
	public void setStatus(Boolean hasUploaded,Boolean hasOperationLog) {
		this.status = getStatus(hasUploaded, hasOperationLog);
	}
	
	public Integer getStatus(Boolean hasUploaded,Boolean hasOperationLog) {
		if(Constants.KeyboardPwdDeleteType.NORMAL != getDeleteType()){
			return Constants.KeyboardPwdStatus.DELETED;
		}
		if( Constants.KeyboardPwdType.PERMANENT == getKeyboardPwdType()  || Constants.KeyboardPwdType.DELETED == getKeyboardPwdType() ) {
			//永久和清空密码
			Boolean hoursTimes = DateUtil.hoursTimes(getCreateDate().getTime(), System.currentTimeMillis(), 24);//true小于24小时，false大于24小时
			if(  hoursTimes ) {
				return  Constants.KeyboardPwdStatus.NORMAL;
			}else if( !hasUploaded ){
				return Constants.KeyboardPwdStatus.UNKNOWN;
			}else {
				if( hasOperationLog) {
					return Constants.KeyboardPwdStatus.NORMAL;
				}else {
					return Constants.KeyboardPwdStatus.INVALID;
				}
			}
		}
		if(Constants.KeyboardPwdType.ONE == getKeyboardPwdType() ) {
			//单次
			Boolean hoursTimes = DateUtil.hoursTimes(getCreateDate().getTime(), System.currentTimeMillis(), 6);//true小于24小时，false大于24小时
			if(  hoursTimes) {
				return Constants.KeyboardPwdStatus.NORMAL;
			}
			else if( !hasUploaded) {
				return Constants.KeyboardPwdStatus.UNKNOWN;
			}
			else {
				return Constants.KeyboardPwdStatus.INVALID;
			}
		}
		
		if( Constants.KeyboardPwdType.DEADLINE == getKeyboardPwdType() ) {
			//限时
			Boolean hoursTimes = DateUtil.hoursTimes( getStartDate(), System.currentTimeMillis(), 24);//true小于24小时，false大于24小时
			
			if(  hoursTimes) {
				return Constants.KeyboardPwdStatus.NORMAL;
			}
			
			else if( ! hasUploaded) {
				return Constants.KeyboardPwdStatus.UNKNOWN;
			}
			
			else {
				
				if( hasOperationLog) {
					return Constants.KeyboardPwdStatus.NORMAL;
				}
				
				else {
					return Constants.KeyboardPwdStatus.INVALID;
				}
				
			}
		}
		
		else {
			//循环
			Boolean hoursTimes = DateUtil.hoursTimes(getStartDate(), System.currentTimeMillis(), 24);//true小于24小时，false大于24小时
			if(  hoursTimes ) {
				return  Constants.KeyboardPwdStatus.NORMAL;
			}else if( !hasUploaded ){
				return Constants.KeyboardPwdStatus.UNKNOWN;
			}else {
				if( hasOperationLog) {
					return Constants.KeyboardPwdStatus.NORMAL;
				}else {
					return Constants.KeyboardPwdStatus.INVALID;
				}
			}
		}
	}
	
	public Integer getStatus() {
		return this.status;
	}
	
//	public Integer getStatus() {
//		if(Constants.KeyboardPwdDeleteType.NORMAL != getDeleteType()){
//			return Constants.KeyboardPwdStatus.DELETED;
//		}
//		
//		//限时，判断是否过了有效期
//		if(Constants.KeyboardPwdType.DEADLINE == getKeyboardPwdType()){
//			if(System.currentTimeMillis() > getEndDate()){
//				return Constants.KeyboardPwdStatus.INVALID;
//			}
//		}
//		
//		if(null != getFirstTime()){ //首次使用时间
//			//单次，首次使用时间不为空时（即已使用），置为失效
//			if(Constants.KeyboardPwdType.ONE == getKeyboardPwdType()){
//				return Constants.KeyboardPwdStatus.INVALID;
//			}
//			//其他的，只要被激活了，就可以继续使用
//			return Constants.KeyboardPwdStatus.NORMAL;
//		}
//
//		//单次,6小时内失效
//		if(Constants.KeyboardPwdType.ONE == getKeyboardPwdType()){
//			Boolean hoursTimes = DateUtil.hoursTimes(getCreateDate().getTime(), System.currentTimeMillis(), 6);
//			return hoursTimes ? Constants.KeyboardPwdStatus.UNKNOWN : Constants.KeyboardPwdStatus.INVALID;
//		}
//		
//		//除了单次，其它类型，24小时内不激活失效
//		Boolean hoursTimes = DateUtil.hoursTimes(getCreateDate().getTime(), System.currentTimeMillis(), 24);
//		return hoursTimes ? Constants.KeyboardPwdStatus.UNKNOWN : Constants.KeyboardPwdStatus.INVALID;
//	}
//	
}
