package com.cosume.ribbon;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.cosume.feign.UserFeignClient;
import com.model.User;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.ribbon.proxy.annotation.Hystrix;

@RestController
@RequestMapping("consume/user")
public class UserController {
	
	Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	UserFeignClient userFeignClient;
	
	@Autowired
	LoadBalancerClient loadBalancerClient;
	
	@HystrixCommand(fallbackMethod = "fallback")
	@RequestMapping("ribbon/get")
	public User getRibbonUser(String id) {
		return restTemplate.getForObject("http://provider/provider/user/get?id="+id, User.class);
	}
	
	public User fallback(String id, Throwable theowable) {
		logger.error("theowable=="+ theowable);
		User user = new User();
		user.setId("aa");
		user.setAge(0);
		user.setName("admin");
		return user;
	}
	
	
	@GetMapping("log-user-instance")
	public void logUserInstance() {
		ServiceInstance choose = this.loadBalancerClient.choose("provider");
		System.out.println(choose.getServiceId());
		System.out.println(choose.getPort());
	}
}
