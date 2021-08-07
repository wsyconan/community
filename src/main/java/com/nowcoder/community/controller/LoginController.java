package com.nowcoder.community.controller;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    // 注册
    @RequestMapping(path="/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    // 登录
    @RequestMapping(path="/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        userService.register(user);
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，请在邮箱中确认激活邮件。");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path="/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int res = userService.activation(userId, code);
        if(res == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功。");
            model.addAttribute("target", "/login");
        } else if(res == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "该账号已经激活过了。");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活码无效，激活失败。");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

}
