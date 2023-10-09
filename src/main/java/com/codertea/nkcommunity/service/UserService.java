package com.codertea.nkcommunity.service;

import com.codertea.nkcommunity.dao.UserMapper;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.util.CommunityConstant;
import com.codertea.nkcommunity.util.CommunityUtil;
import com.codertea.nkcommunity.util.MailClient;
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

}
