package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.nowcoder.community.util.MailClient;

@SpringBootTest
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Test
    public void testTextMain() {
        mailClient.sendMail("2869241810@qq.com", "Test", "Leon has coming!");
    }

}
