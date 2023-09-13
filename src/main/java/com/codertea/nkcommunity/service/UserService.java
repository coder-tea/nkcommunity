package com.codertea.nkcommunity.service;

import com.codertea.nkcommunity.dao.UserMapper;
import com.codertea.nkcommunity.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }
}
