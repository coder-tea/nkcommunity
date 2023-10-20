package com.codertea.nkcommunity.controller;

import com.codertea.nkcommunity.entity.User;
import com.codertea.nkcommunity.service.UserService;
import com.codertea.nkcommunity.util.CommunityUtil;
import com.codertea.nkcommunity.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${project.path.upload}")
    private String uploadPath;

    @Value("${project.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 返回用户设置页面
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    // 处理上传头像的请求
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if(headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        // .png
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "图片的格式不正确！");
            return "/site/setting";
        }
        fileName = CommunityUtil.generateUUID() + suffix;
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！", e);
        }
        // 更新当前用户的头像的路径（web访问路径）
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" +fileName;
        userService.resetHeader(user.getId(), headerUrl);
        // 跳到首页
        return "redirect:/index";
    }

    // 获取服务器本地存放的用户头像
    @RequestMapping(value = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName,
                          HttpServletResponse response) {
        // 在服务器上存放的物理路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀 png
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
            // 输入流是自己创建的，需要手动关闭
            FileInputStream fileInputStream = new FileInputStream(fileName);
        ){
            // 输出流归sprigmvc管理，会帮我们关闭
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取图像失败：" + e.getMessage());
        }

    }
}
