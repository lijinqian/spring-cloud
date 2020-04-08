package com.cosume.feign;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.model.User;

@RestController
@RequestMapping("consume/user")
public class UserFeignController {
Logger logger = Logger.getLogger(this.getClass());
	
	
	@Autowired
	UserFeignClient userFeignClient;
	
	
	@RequestMapping("feign/get")
	public User getFeignUser() {
		Map<String, String> map = new HashMap<>();
		map.put("name", "lijinqian");
		map.put("age", "15");
		User user = userFeignClient.find(map);
//		Assert.notNull(user, "user is not null");
		return user;
	}
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public String uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException {
		System.out.println("consume=="+ file.getSize());
		return userFeignClient.uploadFile(file);
	}
	
}
