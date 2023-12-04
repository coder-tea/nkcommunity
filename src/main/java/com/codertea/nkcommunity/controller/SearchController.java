package com.codertea.nkcommunity.controller;

import com.codertea.nkcommunity.entity.DiscussPost;
import com.codertea.nkcommunity.service.LikeService;
import com.codertea.nkcommunity.service.SearchService;
import com.codertea.nkcommunity.service.UserService;
import com.codertea.nkcommunity.util.CommunityConstant;
import com.codertea.nkcommunity.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // 搜索 search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) throws IOException {
        page.setRows(searchService.findDiscussPostRows(keyword));
        page.setPath("/search?keyword=" + keyword);
        List<DiscussPost> discussPosts = searchService.searchDiscussPost(keyword, page.getCurrent()-1, page.getLimit());
        List<Map<String, Object>> discussPostVOs = new ArrayList<>();
        if(discussPosts!=null) {
            for (DiscussPost discussPost : discussPosts) {
                Map<String, Object> discussPostVO = new HashMap<>();
                // 帖子
                discussPostVO.put("post", discussPost);
                // 作者
                discussPostVO.put("user", userService.findUserById(discussPost.getUserId()));
                // 点赞数量
                discussPostVO.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));
                discussPostVOs.add(discussPostVO);
            }
        }
        model.addAttribute("discussPostVOs", discussPostVOs);
        model.addAttribute("keyword", keyword);
        return "/site/search";
    }
}