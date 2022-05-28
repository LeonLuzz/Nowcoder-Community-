package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SensitiveTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void sensitiveTest() {
        String text = "这里⭐可⭐以⭐赌⭐博，可以⭐嫖❤娼，可以❤吸毒，但我⭐不⭐是⭐⭐台独，哈哈，你们都❤是大傻❤逼，傻瓜❤蛋子！！！";
        text = sensitiveFilter.sensitiveWordFilter(text);
        System.out.println(text);
    }
}
