package com.codertea.nkcommunity.dao;

import com.codertea.nkcommunity.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    // 分页查询评论的目标对象是entityType且目标对象的id是entityId的评论
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // 查询评论的目标对象是entityType且目标对象的id是entityId的评论的总条数
    int selectCountByEntity(int entityType, int entityId);

    // 增加一条评论
    int insertComment(Comment comment);
}
