package com.codertea.nkcommunity.controller;

import com.codertea.nkcommunity.entity.Comment;
import com.codertea.nkcommunity.entity.DiscussPost;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.service.CommentService;
import com.codertea.nkcommunity.service.DiscussPostService;
import com.codertea.nkcommunity.service.UserService;
import com.codertea.nkcommunity.util.HostHolder;
import com.codertea.nkcommunity.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;

@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private DiscussPostService discussPostService;

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

    // 处理查看某人发过的回帖的请求
    @RequestMapping(path = "/mycomment/{userId}", method = RequestMethod.GET)
    public String getMyCommentPage(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if(user==null) {
            throw new RuntimeException("此用户不存在！");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setRows(commentService.findCommentCountByUserId(userId));
        page.setPath("/comment/mycomment/"+userId);
        List<Comment> comments = commentService.findCommentsByUserId(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVOs = new ArrayList<>();
        if(comments!=null) {
            for (Comment comment : comments) {
                Map<String, Object> commentVO = new HashMap<>();
                commentVO.put("comment", comment);
                DiscussPost discussPost = discussPostService.findDiscussPostById(comment.getEntityId());
                commentVO.put("discussPost", discussPost);
                commentVOs.add(commentVO);
            }
        }
        model.addAttribute("commentsNum", commentService.findCommentCountByUserId(userId));
        model.addAttribute("commentVOs", commentVOs);
        return "/site/my-comment";
    }
}
