package com.codertea.nkcommunity.dao;

import com.codertea.nkcommunity.NkcommunityApplication;
import com.codertea.nkcommunity.entity.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NkcommunityApplication.class)
public class DiscussPostMapperTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostMapperTest.class);

    @Test
    public void selectDiscussPosts() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
    }

    @Test
    public void selectDiscussPostRows() {
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testLogger() {
        System.out.println(logger.getName());
        logger.debug("debug log");
        logger.info("debug log");
        logger.warn("debug log");
        logger.error("debug log");
    }
}