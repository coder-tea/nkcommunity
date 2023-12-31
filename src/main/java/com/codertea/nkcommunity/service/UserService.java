package com.codertea.nkcommunity.service;

import com.codertea.nkcommunity.dao.LoginTicketMapper;
import com.codertea.nkcommunity.dao.UserMapper;
import com.codertea.nkcommunity.entity.LoginTicket;
import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.util.CommunityConstant;
import com.codertea.nkcommunity.util.CommunityUtil;
import com.codertea.nkcommunity.util.MailClient;
import com.codertea.nkcommunity.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    // TemplateEngine是thymeleaf的模板引擎核心类
    @Autowired
    private TemplateEngine templateEngine;

    @Value("${project.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /*@Autowired
    private LoginTicketMapper loginTicketMapper;*/

    @Autowired
    private RedisTemplate redisTemplate;


    public User findUserById(int id) {
        /*return userMapper.selectById(id);*/
        // 尝试从缓存里取
        String key = RedisKeyUtil.getUserKey(id);
        User user = (User) redisTemplate.opsForValue().get(key);
        // 从mysql里取,同时初始化缓存
        if(user==null){
            user = userMapper.selectById(id);
            // 往redis里存
            redisTemplate.opsForValue().set(key, user, 3600, TimeUnit.SECONDS);
        }
        return user;
    }

    // 注册的业务逻辑，生成激活码，发送激活邮件
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
            return map;
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
            // 删除缓存
            String key = RedisKeyUtil.getUserKey(userId);
            redisTemplate.delete(key);
            return ACTIVATATION_SUCCESS;
        } else {
            return ACTIVATATION_FAILURE;
        }
    }

    // 登陆的业务逻辑，检查账号密码，生成登录凭证存到数据库，返回登陆凭证
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
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
        /*loginTicketMapper.insertLoginTicket(loginTicket);*/
        String key = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(key, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        /*loginTicketMapper.updateStatus(ticket, 1);*/
        String key = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(key);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(key, loginTicket);
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

    // 忘记密码，修改email对应用户的密码为新密码password
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
        // 删除缓存
        String key = RedisKeyUtil.getUserKey(user.getId());
        redisTemplate.delete(key);
        return map;
    }

    // 根据提供的ticket字符串查找对应的LoginTicket
    public LoginTicket findLoginTicket(String ticket) {
        /*return loginTicketMapper.selectByTicket(ticket);*/
        String key = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(key);
        return loginTicket;
    }

    // 修改用户的头像为用户自主上传的头像
    public int resetHeader(int userId, String headerUrl) {
        /*return userMapper.updateHeader(userId, headerUrl);*/
        int rows = userMapper.updateHeader(userId, headerUrl);
        // 删除缓存
        String key = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(key);
        return rows;
    }

    // 修改user的密码为新密码
    public Map<String, Object> resetPassword(User user, String oldPassword, String newPassword, String confirmPassword) {
        HashMap<String, Object> map = new HashMap<>();
        // 原密码为空，返回到账号设置页面
        if(StringUtils.isBlank(oldPassword)) {
            map.put("oldError", "请输入原密码！");
            return map;
        }
        if(StringUtils.isBlank(newPassword)) {
            map.put("newError", "请输入新密码！");
            return map;
        }
        if(StringUtils.isBlank(confirmPassword)) {
            map.put("confirmError", "请确认新密码！");
            return map;
        }
        if(!newPassword.equals(confirmPassword)) {
            map.put("confirmError", "确认密码和新密码不一致，请重新输入！");
            return map;
        }
        // 检查原密码是否正确
        if(!user.getPassword().equals(CommunityUtil.md5(oldPassword + user.getSalt()))) {
            map.put("oldError", "原密码不正确！");
            return map;
        }
        // 将密码修改为新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(user.getId(), newPassword);
        // 删除缓存
        String key = RedisKeyUtil.getUserKey(user.getId());
        redisTemplate.delete(key);
        return map;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }
}
