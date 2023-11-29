package com.codertea.nkcommunity.service;

import com.codertea.nkcommunity.dao.MessageMapper;
import com.codertea.nkcommunity.entity.Message;
import com.codertea.nkcommunity.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

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

    // 增加消息
    public int addMessage(Message message) {
        // 过滤敏感词
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    // 已读消息，修改他们的状态为已读
    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }

    // 删除信息，修改状态为删除
    public int eraseMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 2);
    }

    // 查询某个主题下userId的最新系统通知
    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    // 查询某个主题下userId的系统通知总数
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    // 查询userId未读的topic主题下系统通知数量 如果topic为null，查询的就是所有topic下未读通知数量的总和
    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    // 查询userId下某个主题所包含的通知列表
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
