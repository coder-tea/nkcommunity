package com.codertea.nkcommunity.controller;

import com.codertea.nkcommunity.entity.Message;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.service.MessageService;
import com.codertea.nkcommunity.service.UserService;
import com.codertea.nkcommunity.util.CommunityUtil;
import com.codertea.nkcommunity.util.HostHolder;
import com.codertea.nkcommunity.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    // 处理查询当前用户朋友私信会话列表的请求
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
    public String getConversationDetail(@PathVariable("conversationId") String conversationId,  Model model, Page page) {
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
        String[] idArray = conversationId.split("_");
        int id1 = Integer.parseInt(idArray[0]);
        int id2 = Integer.parseInt(idArray[1]);
        int targetId = user.getId() == id1 ? id2 : id1;
        User targetUser = userService.findUserById(targetId);
        model.addAttribute("targetUser", targetUser);
        // 设置已读
        List<Integer> ids = getLettersIds(letters);
        if(!ids.isEmpty()) messageService.readMessage(ids);
        return "/site/letter-detail";
    }

    // 传入消息列表，返回其中属于当前用户的未读消息id列表，便于修改对应的消息为已读
    public List<Integer> getLettersIds(List<Message> letters) {
        List<Integer> ids = new ArrayList<>();
        if(letters!=null) {
            for (Message letter : letters) {
                if(hostHolder.getUser().getId() == letter.getToId() && letter.getStatus() == 0) {
                    ids.add(letter.getId());
                }
            }
        }
        return ids;
    }

    // 处理给toName用户发送私信的请求
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        User target = userService.findUserByName(toName);
        if(target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在！");
        }
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        if(message.getFromId()<message.getToId()) {
            message.setConversationId(message.getFromId()+"_"+message.getToId());
        } else {
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    // 处理删除私信的请求
    @RequestMapping(path = "/letter/erase", method = RequestMethod.POST)
    @ResponseBody
    public String eraseLetter(int id) {
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(id);
        messageService.eraseMessage(ids);
        return CommunityUtil.getJSONString(0);
    }
}
