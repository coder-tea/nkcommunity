package com.codertea.nkcommunity.controller.interceptor;

import com.codertea.nkcommunity.entity.LoginTicket;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.service.UserService;
import com.codertea.nkcommunity.util.CookieUtil;
import com.codertea.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

// 请求拦截器
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 在每个请求线程开始之初，就从Coolie里取出凭证然后找到用户，并且把用户存到Threadlocal里面
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if(ticket!=null) {
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效
            if(loginTicket != null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求线程中持有用户，这得考虑并非多线程也没有问题才行，每个线程有自己的存储空间，想到了threadlocal
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    // 在模板引擎之前，把preHandle方法中Threadlocal里存的用户存到Model里
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user!=null && modelAndView!=null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    // 在TemplateEngine之后，请求的响应完成，线程销毁，就清理掉threadlocal里的东西
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
