package com.codertea.nkcommunity.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

// 每次访问service方法都记录访问日志,通过AOP
@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.codertea.nkcommunity.service.*.*(..))")
    public void pointcut() {}

    // 每次访问service方法都记录访问日志
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 浏览器输入http://localhost:8081/nkcommunity/index
        // 用户[0:0:0:0:0:0:0:1],在[2023-10-31 15:08:50],访问了[com.codertea.nkcommunity.service.UserService.findUserById]
        // 浏览器输入http://127.0.0.1:8081/nkcommunity/index
        // 用户[127.0.0.1],在[2023-10-31 15:10:18],访问了[com.codertea.nkcommunity.service.UserService.findUserById]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // ip地址
        String ip = request.getRemoteHost();
        // 时间
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // 类名.方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s],访问了[%s]", ip, now, target));
    }
}
