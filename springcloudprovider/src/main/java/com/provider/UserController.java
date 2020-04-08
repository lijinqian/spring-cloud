package com.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.model.User;
import com.service.UserService;

@RestController
@RequestMapping("provider/user")
public class UserController {
	
	@Autowired
	UserService userService;
	
	@RequestMapping("get")
	public User getUserById(String id) {
		User userById = userService.getUserById(id);
		if(null == userById) {
			throw new RuntimeException();
		}
		return userById;
	}
	
	@RequestMapping("fget")
	public User find(User user) {
		System.out.println("map=="+user.toString());
		return userService.getUserById(user.getId());
	}

}
