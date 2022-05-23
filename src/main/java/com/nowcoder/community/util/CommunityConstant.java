package com.nowcoder.community.util;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
     int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTICATION_FAILURE = 2;

    /**
     * 默认状态登录凭证超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住我时登录凭证超时时间
     */
    int REMEMBERME_EXPIRED_SECONDS = 3600 * 24 * 30;
}
