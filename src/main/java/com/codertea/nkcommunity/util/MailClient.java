package com.codertea.nkcommunity.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {
    public static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    // JavaMailSender是Spring Email的核心组件，负责发送邮件。s
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    // 封装发送邮件的方法
    public void sendMail(String to, String subject, String content) {
        try {
            // MimeMessage用于封装邮件的相关信息
            MimeMessage message = mailSender.createMimeMessage();
            // MimeMessageHelper用于辅助构建MimeMessage对象
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            // 支持html
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败:" + e.getMessage());
        }
    }
}
