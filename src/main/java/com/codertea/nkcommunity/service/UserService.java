package com.codertea.nkcommunity.service;

import com.codertea.nkcommunity.dao.LoginTicketMapper;
import com.codertea.nkcommunity.dao.UserMapper;
import com.codertea.nkcommunity.entity.LoginTicket;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.util.CommunityConstant;
import com.codertea.nkcommunity.util.CommunityUtil;
import com.codertea.nkcommunity.util.MailClient;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${project.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private LoginTicketMapper loginTicketMapper;


    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        // 校验参数并判断是否已经存在
        Map<String, Object> map = new HashMap<>();
        if(user == null)
            throw new IllegalArgumentException("参数不能为空！");
        else if(StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        else if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        else if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }
        User u = userMapper.selectByName(user.getUsername());
        if(u != null) {
            map.put("usernameMsg", "该账号已经存在！请更换一个吧");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null) {
            map.put("emailMsg", "该邮箱已经被注册！");
        }
        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("url", domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode());
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    public int activation(int userId, String activationCode) {
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1) return ACTIVATATION_REPEAT;
        else if(user.getActivationCode().equals(activationCode)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATATION_SUCCESS;
        } else {
            return ACTIVATATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password,
                                     int expiredSeconds) {
        // expiredSeconds是本次登录的有效时间，单位是秒
        HashMap<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }
        // 验证激活状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }
        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if(!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }
        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    // 验证邮箱的有效性，然后给邮箱发送一封邮件，内容是忘记密码后修改时需要输入的验证码
    public Map<String, Object> getForgetCode(String email) {
        HashMap<String, Object> map = new HashMap<>();
        if(StringUtils.isBlank(email)) {
            return map;
        }
        User user = userMapper.selectByEmail(email);
        if(user == null) {
            return map; // 该账号未注册
        } else if(user.getStatus()==0) {
            return map; // 该账号未激活
        }
        // 发送邮箱验证邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String forgetCode = CommunityUtil.generateUUID().substring(0, 4);
        context.setVariable("forgetCode", forgetCode);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(user.getEmail(), "找回密码", content);
        map.put("forgetCode", forgetCode);
        return map;
    }

    // 修改email对应用户的密码为新密码password
    public Map<String, Object> resetPassword(String email, String password) {
        HashMap<String, Object> map = new HashMap<>();
        if(StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("password", "新密码不能为空！");
            return map;
        }
        User user = userMapper.selectByEmail(email);
        if(user == null) {
            map.put("emailMsg", "该邮箱尚未注册！");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);
        map.put("user", user);
        return map;
    }
}






