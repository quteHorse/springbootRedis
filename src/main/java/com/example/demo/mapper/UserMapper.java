package com.example.demo.mapper;

import com.example.demo.model.UserInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("UserMapper")

public interface UserMapper {

    List<UserInfo> queryAll();

    UserInfo findUserById(int id);

    int updateUser(UserInfo user);

    int deleteUserById(int id);
}
