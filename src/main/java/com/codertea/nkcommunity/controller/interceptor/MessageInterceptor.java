package com.codertea.nkcommunity.controller.interceptor;

import com.codertea.nkcommunity.entity.LoginTicket;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.service.MessageService;
import com.codertea.nkcommunity.util.CookieUtil;
import com.codertea.nkcommunity.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    // 在模板引擎之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user!=null && modelAndView!=null) {
            int unreadLetterCount = messageService.findUnreadLetterCount(user.getId(), null);
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount", unreadLetterCount+noticeUnreadCount);
        }
    }
}
