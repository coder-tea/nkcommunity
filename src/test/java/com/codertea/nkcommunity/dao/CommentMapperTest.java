package com.codertea.nkcommunity.dao;

import com.codertea.nkcommunity.NkcommunityApplication;
import com.codertea.nkcommunity.entity.Comment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NkcommunityApplication.class)
public class CommentMapperTest {
    @Autowired
    private CommentMapper commentMapper;
}