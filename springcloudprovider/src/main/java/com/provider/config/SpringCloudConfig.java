package com.provider.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * spring cloud config
 * 从git拉取配置，生效类
 * @author ljq
 * @Date 2020年4月7日
 * @company 深沐恩
 */
@Configuration
public class SpringCloudConfig { 
  
	/**
	 * 拉取git配置文件，使用的jdbc-url
	 * 本地配置文件使用的url
	 * 如果使用本地配置文件，需要把这个方法注掉
	 * @return
	 */
  @Bean
  @Primary
  @RefreshScope// 刷新配置文件 
  @ConfigurationProperties(prefix="spring.datasource") // 数据源的自动配置的前缀 
  public DataSource dataSource(){ 
    return DataSourceBuilder.create().build(); 
  }
  
  
  
}
