package com.codertea.nkcommunity.dao;

import com.codertea.nkcommunity.NkcommunityApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NkcommunityApplication.class)
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void selectById() {
        System.out.println(userMapper.selectById(11));
    }

    @Test
    public void selectByName() {
    }

    @Test
    public void selectByEmail() {
    }

    @Test
    public void insertUser() {
    }

    @Test
    public void updateStatus() {
    }

    @Test
    public void updateHeader() {
    }

    @Test
    public void updatePassword() {
    }
}