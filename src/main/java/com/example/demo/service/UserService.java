package com.example.demo.service;

import com.example.demo.model.UserInfo;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    public UserInfo save(UserInfo user);

    public void delete(int id);

    public UserInfo get(Integer id);

    public UserInfo findUserById(int id);

    public int updateUser(UserInfo userInfo);

    public int deleteUserById(int id);
}
