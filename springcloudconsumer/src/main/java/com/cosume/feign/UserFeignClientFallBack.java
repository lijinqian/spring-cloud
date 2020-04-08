package com.cosume.feign;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.model.User;

@Component
public class UserFeignClientFallBack implements UserFeignClient {

	@Override
	public User find(Map<String, String> map) {
		User user = new User();
		user.setId("0");
		user.setAge(0);
		user.setName("admin");
		return user;
	}

	@Override
	public String uploadFile(MultipartFile file) {
		return "上传文件错误";
	}

}
