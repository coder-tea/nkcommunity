package com.codertea.nkcommunity.util;

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
public class SensitiveFilterTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void filter() {
        String text = "这里可以赌博,可以嫖娼,可以吸毒,可以开票,哈哈哈!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "这里可以☆赌☆博☆,可以☆嫖☆娼☆,可以☆吸☆毒☆,可以☆开☆票☆,哈哈哈!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "*你开&票#哈哈";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "*你开&的票#哈哈";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
