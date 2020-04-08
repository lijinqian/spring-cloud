package com.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import com.model.User;
import tk.mybatis.mapper.common.Mapper;

public interface UserMapper extends Mapper<User>{

	@Select("select * from user where id = #{id}")
	User getUserById(@Param("id") String id);

}
