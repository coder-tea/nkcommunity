package com.codertea.nkcommunity.controller;


import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.service.UserService;
import com.codertea.nkcommunity.util.CommunityConstant;
import com.codertea.nkcommunity.util.CommunityUtil;
import com.codertea.nkcommunity.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {


    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    // 返回注册的页面
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    // 返回登录页面
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    // 注册请求
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    // 点击激活邮件中的激活地址，激活账号的请求
    @RequestMapping(path = "/activation/{userId}/{activationCode}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,
                             @PathVariable("activationCode") String activationCode) {
        int res = userService.activation(userId, activationCode);
        if(res == ACTIVATATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if(res == ACTIVATATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经激活过了！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    // 获取验证码图片，同时后端session存储验证码的文字
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 为什么是void，因为返回不是一个字符串，而是图片，所以手动response
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        /*// 将验证码文字存入session
        session.setAttribute("kaptcha", text);*/
        // -------存到redis里
        // 标识用户的随机字符串
        String owner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("owner", owner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 验证码存到redis里
        String key = RedisKeyUtil.getKaptchaKey(owner);
        redisTemplate.opsForValue().set(key, text, 60, TimeUnit.SECONDS);
        // 将图片输出给浏览器
        response.setContentType("image/png");
        // OutputStream是字节流，图片用这个比较好，writer是字符流，文字用writer比较好
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
            // response不用手动关，是由springmvc维护的，拿来用就行了
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }

    // 处理在登录页面发起的登录请求
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model/*, HttpSession session*/, HttpServletResponse response,
                        @CookieValue("owner") String owner) {
         // 检查验证码 比较session里的验证码和传入的验证码是否一样
        /*String kaptcha = (String) session.getAttribute("kaptcha");*/
        String kaptcha = null;
        if(StringUtils.isNotBlank(owner)) {
            kaptcha = (String) redisTemplate.opsForValue().get(RedisKeyUtil.getKaptchaKey(owner));
        } else {
            model.addAttribute("codeMsg", "验证码过期了!");
            return "/site/login";
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }
        // 检查账号,密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    // 处理退出请求
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }

    // 打开忘记密码页面
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "/site/forget";
    }

    // 在忘记密码页面的表单中输入注册的邮箱，点击获取验证码按钮，服务器为该邮箱发送一份验证码
    @RequestMapping(path = "/forget/code", method = RequestMethod.GET)
    @ResponseBody
    public String getForgetCode(String email, HttpSession session) {
        if(StringUtils.isBlank(email)) return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        Map<String, Object> map = userService.getForgetCode(email);
        if(map.containsKey("forgetCode")) {
            // TODO:如何设置失效时间？
            //  验证码存在共享问题(且不论啥分布式session同步不同步)
            // 问题描述：忘记密码页面中，输入邮箱A，获取验证码；此时将邮箱改为B，输入邮箱A获取的验证码，此时可以修改密码
            // 此处问题主要在于LoginController.java下，getForgetCode方法和resetPassword方法中，存与取验证码都是基于seesion的key("verifyCode")，未作用户区分，简单进行修改即可，比如
            // session.setAttribute(email+"_verifyCode", code);resetPassword方法下改为String code = (String) session.getAttribute(email+"_verifyCode");
            session.setAttribute("forgetCode", map.get("forgetCode"));
            return CommunityUtil.getJSONString(0);
        } else {
            return CommunityUtil.getJSONString(1, "查询不到该邮箱注册信息");
        }
    }

    // 输入邮箱、验证码、新密码，进行密码重置
    @RequestMapping(path = "/forget/reset", method = RequestMethod.POST)
    public String resetPassword(String email, String forgetCode, String password, Model model, HttpSession session) {
        String trueForgetCode = (String) session.getAttribute("forgetCode");
        if (StringUtils.isBlank(trueForgetCode) || StringUtils.isBlank(forgetCode) || !trueForgetCode.equalsIgnoreCase(forgetCode)) {
            model.addAttribute("forgetCodeMsg", "验证码错误!");
            return "/site/forget";
        }

        Map<String, Object> map = userService.resetPassword(email, password);
        if (map.containsKey("user")) {
            model.addAttribute("msg","修改密码成功，请重新登录");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        } else {
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }
}

