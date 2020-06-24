package com.igloo.smarthome;

/**
 * alexa response_type always use code type;
 * @author butterfly
 *
 */
public class OAuth2Bean {
	String client_id;
	String redirect_uri="";
	String response_type;
	String scope;
	String state;
	String client_secret = "";
	public static String CLIENT_ID   =  "174726b6011749269c2ee2d1331330a7";
	public static String APPSECRET=  "849fb03858df13837a5a2b40f14f4304";
	String grant_type;
	String code;
	
	
	
	
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "OAuth2Bean [client_id=" + client_id + ", redirect_uri=" + redirect_uri + ", response_type="
				+ response_type + ", scope=" + scope + ", state=" + state + ", client_secret="
				+ client_secret + ", grant_type=" + grant_type + ", code=" + code + "]";
	}
	
	public String getClient_secret() {
		return client_secret;
	}
	public void setClient_secret(String client_secret) {
		this.client_secret = client_secret;
	}
	public String getCLIENT_ID() {
		return CLIENT_ID;
	}
	public void setCLIENT_ID(String cLIENT_ID) {
		CLIENT_ID = cLIENT_ID;
	}
	public String getAPPSECRET() {
		return APPSECRET;
	}
	public void setAPPSECRET(String aPPSECRET) {
		APPSECRET = aPPSECRET;
	}
	public String getGrant_type() {
		return grant_type;
	}
	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getRedirect_uri() {
		return redirect_uri;
	}
	public void setRedirect_uri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}
	public String getResponse_type() {
		return response_type;
	}
	public void setResponse_type(String response_type) {
		this.response_type = response_type;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

	
	
}
