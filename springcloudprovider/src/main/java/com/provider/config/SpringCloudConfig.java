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
  
  @Bean
  @Primary
  @RefreshScope// 刷新配置文件 
  @ConfigurationProperties(prefix="spring.datasource") // 数据源的自动配置的前缀 
  public DataSource dataSource(){ 
    return DataSourceBuilder.create().build(); 
  }
  
  
  
}
