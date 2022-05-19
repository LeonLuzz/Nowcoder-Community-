package com.nowcoder.community.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5
    // 密码加上一个随机字符串 -> hello + 3e4a8
    public static String MD5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        // 传入的结果加密成16进制的数字
        return org.springframework.util.DigestUtils.md5DigestAsHex(key.getBytes());
    }


}
