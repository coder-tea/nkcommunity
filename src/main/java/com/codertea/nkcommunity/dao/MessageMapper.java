package com.codertea.nkcommunity.dao;

import com.codertea.nkcommunity.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
     // 分页查询当前用户的会话列表，针对每个会话只返回一条最新的私信,用作展示
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 分页查询某个会话所包含的所有私信
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信总数
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量。如果传入conversationId就查询当前用户的某一个会话中未读私信的数量；否则就查询当前用户所有会话中未读私信的数量
    int selectUnreadLetterCount(int userId, String conversationId);

    // 增加消息
    int insertMessage(Message message);

    // 批量更改多条消息的状态
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题下userId的最新系统通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题下userId的系统通知总数
    int selectNoticeCount(int userId, String topic);

    // 查询userId未读的topic主题下系统通知数量 如果topic为null，查询的就是所有topic下未读通知数量的总和
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询userId下某个主题所包含的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
