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
}
