package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
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

    // 封装json数据
    public static String getJsonString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJsonString(int code, String msg) {
        return getJsonString(code, msg, null);
    }

    public static String getJsonString(int code) {
        return getJsonString(code, null, null);
    }
}
