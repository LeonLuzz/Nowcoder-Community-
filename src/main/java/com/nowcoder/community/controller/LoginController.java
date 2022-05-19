package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController {
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "site/register";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
       Map<String, Object> map =  userService.register(user);
       if (map == null || map.isEmpty()) {
            // 注册成功，跳转到首页
           model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮箱，请尽快激活！");
           model.addAttribute("target", "/idnex");
           return "/site/poerate-result";
       } else {
           model.addAttribute("usernameMsg", map.get("usernameMsg"));
           model.addAttribute("passwordMsg", map.get("passwordMsg"));
           model.addAttribute("emailMsg", map.get("emailMsg"));
           return "site/register";
       }
    }

}
