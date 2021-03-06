package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.BufferPoolMXBean;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Producer kaptcha;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

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

    // 生成验证码并由session传递
    @RequestMapping(path="/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = kaptcha.createText();
        BufferedImage image = kaptcha.createImage(text);

        // 将验证码存入session
        // session.setAttribute("kaptcha", text);

        // 首先生成Cookie,将验证码放入其中
        String captchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("captchaOwner", captchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getCaptchaKey(captchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出到浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }

    //
    @RequestMapping(path = "login", method = RequestMethod.POST)
    public String login(String username, String password, String code,
                        boolean rememberMe, Model model, HttpSession session, HttpServletResponse response,
                        @CookieValue("captchaOwner") String captchaOwner) {
        // 检查验证码
        //String captcha = (String) session.getAttribute("kaptcha");
        // 从Cookie中取得验证码并验证
        String captcha = null;
        if(StringUtils.isNotBlank(captchaOwner)) {
            String redisKey = RedisKeyUtil.getCaptchaKey(captchaOwner);
            captcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if(StringUtils.isBlank(captcha) || StringUtils.isBlank(code) || !captcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码错误。");
            return "/site/login";
        }

        // 检查账号，密码
        int expiredSeconds = rememberMe? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    // 退出登录
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

}
