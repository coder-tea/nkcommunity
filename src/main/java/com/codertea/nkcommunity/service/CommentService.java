package com.codertea.nkcommunity.service;

import com.codertea.nkcommunity.dao.CommentMapper;
import com.codertea.nkcommunity.entity.Comment;
import com.codertea.nkcommunity.util.CommunityConstant;
import com.codertea.nkcommunity.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    // 分页查询评论的目标对象是entityType且目标对象的id是entityId的评论
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    // 查询评论的目标对象是entityType且目标对象的id是entityId的评论的总条数
    public int findCommentCountByEntity(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    // 增加评论,包括两个DML操作：增加评论和更新帖子的评论数量，要么全成功要么全失败
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if(comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        // 过滤敏感词
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        // 增加评论
        int rows = commentMapper.insertComment(comment);
        // 如果是直接给帖子的评论，而不是评论的回复，则更新帖子的评论数量
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            int curCount = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.resetCommentCount(comment.getEntityId(), curCount);
        }
        return rows;
    }
}
