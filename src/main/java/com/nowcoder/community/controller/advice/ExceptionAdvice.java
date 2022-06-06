package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class}) // 方法必须公有且没有返回值
    public void handleException(Exception e, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.error("服务器发生异常: " + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        // 判断普通请求和异步请求
        String xRequestedWith = req.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {   // 异步请求
            resp.setContentType("application/plain; charset=utf-8"); // 返回普通的字符串，需要手动转换为json
            PrintWriter writer = resp.getWriter();
            writer.write(CommunityUtil.getJsonString(1, "服务器异常！"));
        } else {
            resp.sendRedirect(req.getContextPath() + "/error");
        }
    }
}
