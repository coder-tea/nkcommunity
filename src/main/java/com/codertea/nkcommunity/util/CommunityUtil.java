package com.codertea.nkcommunity.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // md5加密 hello->abc123  md5加盐加密 hello+123->abc123tyx
    public static String md5(String key) {
        if(StringUtils.isBlank(key)) return null;
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
