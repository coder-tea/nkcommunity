package com.codertea.nkcommunity.util;

import com.codertea.nkcommunity.entity.User;
import org.springframework.stereotype.Component;

/**
 * 基于threadlocal，持有用户信息，每个线程可以隔离
 * */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    // 把当前线程的user存到threadlocal里
    public void setUser(User user) {
        users.set(user);
    }
    public User getUser() {
        return users.get();
    }
    // 请求结束时清理当前线程的threadlocal防止占用内存。、
    // 那么线程什么时候结束？当服务器对浏览器作出响应后，线程才被销毁。
    public void clear() {
        users.remove();
    }
}
