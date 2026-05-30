package com.ms.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.server.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username = #{username} AND password = #{password} LIMIT 1")
    User selectByUsernameAndPassword(@Param("username") String username, @Param("password") String password);

    @Select("SELECT * FROM users WHERE username = #{username} LIMIT 1")
    User selectByUsername(@Param("username") String username);
}
