package com.codertea.nkcommunity.controller.interceptor;

import com.codertea.nkcommunity.annotation.LoginRequired;
import com.codertea.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

// 拦截带有LoginRequired注解的方法，然后判断是否登陆，登录了就可以访问，否则就拒绝
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 拦截器会拦截包括静态资源、方法等在内的所有请求，所以要先判断handler是不是方法
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 带有LoginRequired注解，需要登录才能访问
            if(loginRequired != null) {
                // 没登录
                if(hostHolder.getUser() == null) {
                    // 强制跳转到登录页面
                    // 其实controller里面的重定向的底层也是response.sendRedirect这么写的
                    // 在Controller的方法里，可以通过返回以”redirect”开头的字符串实现重定向;在Controller的方法里，也可以通过response对象的sendRedirect方法实现重定向
                    // 在拦截器中，只能通过response对象的sendRedirect方法实现重定向
                    response.sendRedirect(request.getContextPath() + "/login");
                    return false;
                }
            }
        }
        return true;
    }
}
