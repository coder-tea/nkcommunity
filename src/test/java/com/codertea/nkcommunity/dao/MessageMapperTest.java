package com.codertea.nkcommunity.dao;

import com.codertea.nkcommunity.NkcommunityApplication;
import com.codertea.nkcommunity.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = NkcommunityApplication.class)
public class MessageMapperTest {
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void selectConversations() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }
    }

    @Test
    public void selectConversationCount() {
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);
    }

    @Test
    public void selectLetters() {
        List<Message> messages = messageMapper.selectLetters("111_112", 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }
    }

    @Test
    public void selectLetterCount() {
        int count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);
    }

    @Test
    public void selectUnreadLetterCount() {
        int i = messageMapper.selectUnreadLetterCount(131, "111_131");
        System.out.println(i);
        int i1 = messageMapper.selectUnreadLetterCount(131, null);
        System.out.println(i1);
    }
}