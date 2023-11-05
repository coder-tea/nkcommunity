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

    // 找到某个user发表的某一类型的全部回复
    List<Comment> selectCommentsByUserId(int userId, int entityType, int offset, int limit);

    // 查询某个user发表的某一类型的全部回复的数量
    int selectCountByUserId(int userId, int entityType);
}
