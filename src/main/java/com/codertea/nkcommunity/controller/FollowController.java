package com.codertea.nkcommunity.controller;


import com.codertea.nkcommunity.entity.Event;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.event.EventProducer;
import com.codertea.nkcommunity.service.FollowService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    // 处理关注的请求
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if(user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注！");
    }

    // 处理取消关注的请求
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if(user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注！");
    }

    // 处理关注列表页面的请求
    @RequestMapping(path = "/followee/{userId}", method = RequestMethod.GET)
    public String getFolloweePage(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if(user==null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followee/"+userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        List<Map<String, Object>> followeeVOs = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if(followeeVOs!=null) {
            for (Map<String, Object> followeeVO : followeeVOs) {
                User followeeUser = (User) followeeVO.get("followeeUser");
                boolean hasFollowed = false; // 当前登陆用户是否关注了此用户
                if(hostHolder.getUser()!=null) {
                    hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, followeeUser.getId());
                }
                followeeVO.put("hasFollowed",  hasFollowed);
            }
        }
        model.addAttribute("followeeVOs", followeeVOs);
        return "/site/followee";
    }

    // 处理粉丝列表页面的请求
    @RequestMapping(path = "/follower/{userId}", method = RequestMethod.GET)
    public String getFollowerPage(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if(user==null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/follower/"+userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));
        List<Map<String, Object>> followerVOs = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(followerVOs!=null) {
            for (Map<String, Object> followerVO : followerVOs) {
                User followerUser = (User) followerVO.get("followerUser");
                boolean hasFollowed = false; // 当前登陆用户是否关注了此用户
                if(hostHolder.getUser()!=null) {
                    hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, followerUser.getId());
                }
                followerVO.put("hasFollowed",  hasFollowed);
            }
        }
        model.addAttribute("followerVOs", followerVOs);
        return "/site/follower";
    }
}
