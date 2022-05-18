package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User selectById(@Param("id") Long id);
    User selectByUsername(@Param("username") String username);
    User selectByEmail(@Param("email") String email);
    int insertUser(User user);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int updateHeaderUrl(@Param("id") Long id, @Param("headerUrl") String headerUrl);
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}

