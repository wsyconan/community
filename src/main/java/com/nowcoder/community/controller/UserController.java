package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "请上传图片。");
            return "/site/setting";
        }

        // 检查文件格式
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确。");
            return "/site/setting";
        }

        // 生成随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放路径
        File file = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("头像储存失败" + e.getMessage());
            throw new RuntimeException("头像储存失败", e);
        }

        // 更新当前用户头像路径（web访问路径）
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 找到服务器存放的文件
        fileName = uploadPath + "/" + fileName;
        // 向服务器输出文件
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("/image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(Model model, String oldPassword, String newPassword, String confirmPassword) {
        // 检查空输入
        if(StringUtils.isBlank(oldPassword)) {
            model.addAttribute("oldPasswordError", "请输入旧的密码。");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)) {
            model.addAttribute("newPassword1Error", "请输入新的密码。");
            return "/site/setting";
        }
        if(StringUtils.isBlank(confirmPassword)) {
            model.addAttribute("newPassword2Error", "请确认新的密码。");
            return "/site/setting";
        }

        int userId = hostHolder.getUser().getId();
        // 检查旧密码是否正确
        if(!userService.checkPassword(userId, oldPassword)) {
            model.addAttribute("oldPasswordError", "密码不正确。");
            return "/site/setting";
        }

        // 检查新密码是否一致
        if(newPassword.equals(oldPassword)) {
            model.addAttribute("newPassword1Error", "新密码不能与旧密码相同。");
            return "/site/setting";
        }
        if(!newPassword.equals(confirmPassword)) {
            model.addAttribute("newPassword2Error", "新密码不一致。");
            return "/site/setting";
        }

        // 更新用户密码
        userService.updatePassword(userId, newPassword);

        return "redirect:/index";
    }

}