package com.codertea.nkcommunity.service;

import com.codertea.nkcommunity.dao.CommentMapper;
import com.codertea.nkcommunity.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    // 分页查询评论的目标对象是entityType且目标对象的id是entityId的评论
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    // 查询评论的目标对象是entityType且目标对象的id是entityId的评论的总条数
    public int findCommentCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }
}
