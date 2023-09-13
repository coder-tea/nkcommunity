package com.codertea.nkcommunity.controller;

import com.codertea.nkcommunity.entity.DiscussPost;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.service.DiscussPostService;
import com.codertea.nkcommunity.service.UserService;
import com.codertea.nkcommunity.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        System.out.println(page);
        // 方法调用前，SpringMVC会自动实例化Model和Page，并将Page注入Model，所以，
        // 在Thymeleaf中可以直接访问Page对象中的数据，而不需要专门model.addAttribute(page)了
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> res = new ArrayList<>();
        if(discussPosts != null) {
            for (DiscussPost discussPost : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);
                res.add(map);
            }
        }
        model.addAttribute("discussPosts", res);
        return "/index";
    }
}
