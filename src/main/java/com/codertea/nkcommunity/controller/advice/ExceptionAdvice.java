package com.codertea.nkcommunity.controller.advice;

import com.codertea.nkcommunity.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// 统一处理异常
// @ControllerAdvice这个注解表示只去作用于带有Controller注解的那些bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    // Exception.class是所有异常的父类，表示所有异常我都处理
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 记录概括日志
        logger.error("服务器发生异常："+e.getMessage());
        // 记录详细日志
        for(StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        String xRequestedWith = request.getHeader("x-requested-with");
        // 如果是异步请求
        if("XMLHttpRequest".equals(xRequestedWith)) {
            // 返回一个普通字符串，前端拿到以后用parseJSON解析成json对象
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常！"));
        // 否则是模板引擎同步请求,就重定向到错误页面
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
