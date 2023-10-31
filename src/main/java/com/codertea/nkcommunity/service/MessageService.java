package com.codertea.nkcommunity.service;

import com.codertea.nkcommunity.dao.MessageMapper;
import com.codertea.nkcommunity.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    // 分页查询当前用户的会话列表，针对每个会话只返回一条最新的私信,用作展示
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    // 查询当前用户的会话数量
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }


    // 分页查询某个会话所包含的所有私信
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    // 查询某个会话所包含的私信总数
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    // 查询未读私信的数量。如果传入conversationId就查询当前用户的某一个会话中未读私信的数量；否则就查询当前用户所有会话中未读私信的数量
    public int findUnreadLetterCount(int userId, String conversationId) {
        return messageMapper.selectUnreadLetterCount(userId, conversationId);
    }
}
