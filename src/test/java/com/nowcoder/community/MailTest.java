package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.nowcoder.community.util.MailClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMain() {
        mailClient.sendMail("2869241810@qq.com", "Test", "Leon has coming!");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "Hello world!");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("2869241810@qq.com", "HTML", content);
    }
}
