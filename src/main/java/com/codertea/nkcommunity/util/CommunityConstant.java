package com.codertea.nkcommunity.util;

public interface CommunityConstant {
    /**
     * 账号激活成功
     * */
    int ACTIVATATION_SUCCESS = 0;
    /**
     * 重复激活（多次点击激活链接）
     * */
    int ACTIVATATION_REPEAT = 1;
    /**
     * 激活失败
     * */
    int ACTIVATATION_FAILURE = 2;
    /**
     * 默认的登录凭证的超时时间，以秒为单位 12个小时
     */
    int DEFAULT_EXPIRED_SECONDS = 12 * 60 * 60;
    /**
     * 勾选记住我的登录凭证的超时时间，以秒为单位 100天
     */
    int REMEMBER_EXPIRED_SECONDS = 100 * 24 * 60 * 60;

    /**
     * 实体类型 帖子
     * */
    int ENTITY_TYPE_POST = 1;
    /**
     * 实体类型 评论
     * */
    int ENTITY_TYPE_COMMENT = 2;
    /**
     * 实体类型 用户
     * */
    int ENTITY_TYPE_USER = 3;
}
