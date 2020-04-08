package com;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 添加该配置，客户端才能连上开启用户认证的注册中心
 * @author ljq
 * @Date 2020年3月13日
 * @company 深沐恩
 */
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		 http.csrf().disable();
        super.configure(http);
	}
	
}
