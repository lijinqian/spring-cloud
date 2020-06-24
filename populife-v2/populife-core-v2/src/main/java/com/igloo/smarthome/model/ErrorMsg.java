package com.igloo.smarthome.model;

import javax.persistence.Id;

public class ErrorMsg {
	@Id
	Integer id;
	String userid;
	String msg;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String toString() {
		return "ErrorMsg [id=" + id + ", userid=" + userid + ", msg=" + msg + "]";
	}
	
	
}
