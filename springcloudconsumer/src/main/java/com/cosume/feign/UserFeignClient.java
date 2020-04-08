package com.cosume.feign;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.cosume.feign.UserFeignClient.UploadFileSupportConfig;
import com.model.User;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;

@FeignClient(name="provider", fallback = UserFeignClientFallBack.class, configuration = UploadFileSupportConfig.class)
public interface UserFeignClient {
	
	@RequestMapping(value = "/provider/user/fget", method = RequestMethod.GET)
	public User find(@RequestParam Map<String, String> map);
	
	@RequestMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadFile(@RequestPart(value = "file") MultipartFile file);
	
	
	public class UploadFileSupportConfig {
	  @Bean
	  public Encoder feignFormEncoder() {
	    return new SpringFormEncoder();
	  }
	}
}
