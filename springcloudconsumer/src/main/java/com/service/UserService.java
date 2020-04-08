package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mapper.UserMapper;
import com.model.User;

@Service
public class UserService {
	
	@Autowired
	UserMapper userMapper;

	public User getUserById(String id) {
		return userMapper.getUserById(id);
	}
}
