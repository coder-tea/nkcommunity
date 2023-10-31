package com.codertea.nkcommunity.controller;

import com.codertea.nkcommunity.entity.Message;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.service.MessageService;
import com.codertea.nkcommunity.service.UserService;
import com.codertea.nkcommunity.util.HostHolder;
import com.codertea.nkcommunity.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 处理查询当前用户朋友私信列表的请求
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        // 会话列表
        List<Message> conversations = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversationVOs = new ArrayList<>();
        for (Message conversation : conversations) {
            HashMap<String, Object> conversationVO = new HashMap<>();
            conversationVO.put("conversation", conversation);
            conversationVO.put("letterCount", messageService.findLetterCount(conversation.getConversationId()));
            // 当前
            conversationVO.put("unreadCount", messageService.findUnreadLetterCount(user.getId(), conversation.getConversationId()));
            // 私信对象的id,用于显示会话对象的头像和用户名
            int targetId = user.getId() == conversation.getFromId() ? conversation.getToId() : conversation.getFromId();
            conversationVO.put("targetUser", userService.findUserById(targetId));
            conversationVOs.add(conversationVO);
        }
        model.addAttribute("conversationVOs", conversationVOs);
        // 查询当前用户所有未读私信数量
        int unreadLetterCount = messageService.findUnreadLetterCount(user.getId(), null);
        model.addAttribute("unreadLetterCount", unreadLetterCount);
        // 返回私信会话列表页面
        return "/site/letter";
    }

    // 处理查询某一个会话中全部消息列表的请求
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String get(@PathVariable("conversationId") String conversationId,  Model model, Page page) {
        User user = hostHolder.getUser();
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        // 某个私信会话的消息列表
        List<Message> letters = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letterVOs = new ArrayList<>();
        for (Message letter : letters) {
            HashMap<String, Object> letterVO = new HashMap<>();
            letterVO.put("letter", letter);
            letterVO.put("fromUser", userService.findUserById(letter.getFromId()));
            letterVOs.add(letterVO);
        }
        model.addAttribute("letterVOs", letterVOs);
        // 私信对象的id,用于显示会话对象的头像和用户名
        String[] ids = conversationId.split("_");
        int id1 = Integer.parseInt(ids[0]);
        int id2 = Integer.parseInt(ids[1]);
        int targetId = user.getId() == id1 ? id2 : id1;
        User targetUser = userService.findUserById(targetId);
        model.addAttribute("targetUser", targetUser);
        return "/site/letter-detail";
    }
}
