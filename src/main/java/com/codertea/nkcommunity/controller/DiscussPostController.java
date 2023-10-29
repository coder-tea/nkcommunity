package com.codertea.nkcommunity.controller;

import com.codertea.nkcommunity.entity.Comment;
import com.codertea.nkcommunity.entity.DiscussPost;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.service.CommentService;
import com.codertea.nkcommunity.service.DiscussPostService;
import com.codertea.nkcommunity.service.UserService;
import com.codertea.nkcommunity.util.CommunityConstant;
import com.codertea.nkcommunity.util.CommunityUtil;
import com.codertea.nkcommunity.util.HostHolder;
import com.codertea.nkcommunity.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discusspost")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    // 处理发布帖子的异步请求
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if(user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setType(0);
        post.setStatus(0);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    // 处理查看帖子详情的请求，根据帖子id
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPostDetail(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子内容
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 需要把发帖人的信息也找出来，有两种思路，一是在mybatis mapper里查帖子时写关联查询，效率高但是耦合；二是在这里用帖子id做关联查询。耦合度低
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 分页显示评论
        page.setLimit(5);
        page.setPath("/discusspost/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // VO view opbject其实就是一个Map，包含键值对，所以可以抽象地看成是一个对象
        // 1.评论列表（评论：给帖子的评论；回复：给评论的评论）
        List<Comment> comments = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 1.为了把评论列表显示出来，我们需要转成VO列表
        List<Map<String, Object>> commentVOs = new ArrayList<>();
        if(comments != null) {
            for (Comment comment : comments) {
                // 2.commentVO
                Map<String, Object> commentVO = new HashMap<>();
                // 2.给commentVO加入comment
                commentVO.put("comment", comment);
                // 2.给commentVO加入作者
                commentVO.put("user", userService.findUserById(comment.getUserId()));
                // 2.给commentVO加入回复数量
                int replyCount = commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVO.put("replyCount", replyCount);
                // 2.给commentVO加入回复 不分页
                List<Comment> replys = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 2.为了把回复列表显示出来，我们需要转成VO列表
                List<Map<String, Object>> replyVOs = new ArrayList<>();
                if(replys!=null) {
                    for (Comment reply : replys) {
                        // 3.replyVO
                        Map<String, Object> replyVO = new HashMap<>();
                        // 3.给replyVO加入reply
                        replyVO.put("reply", reply);
                        // 3.给replyVO加入user
                        replyVO.put("user", userService.findUserById(reply.getUserId()));
                        // 3.给replyVO加入target回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVO.put("target", target);
                        // 3.把replyVO加到replyVOs里
                        replyVOs.add(replyVO);
                    }
                }
                // 2.把
                commentVO.put("replyVOs", replyVOs);
                // 2.把commentVO加到commentVOs里
                commentVOs.add(commentVO);
            }
        }
        // 1.model加入评论Vo列表
        model.addAttribute("commentVOs", commentVOs);
        return "/site/discuss-detail";
    }
}
