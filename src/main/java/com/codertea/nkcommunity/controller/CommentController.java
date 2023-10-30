package com.codertea.nkcommunity.controller;

import com.codertea.nkcommunity.entity.Comment;
import com.codertea.nkcommunity.service.CommentService;
import com.codertea.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    // 处理在帖子详情页面点击增加评论的请求，完成后还要重定向到当前的帖子详情页面，所以要把帖子id也传过来
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        // 补充comment的其他字段
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        // 添加
        commentService.addComment(comment);
        return "redirect:/discusspost/detail/"+discussPostId;
    }
}
