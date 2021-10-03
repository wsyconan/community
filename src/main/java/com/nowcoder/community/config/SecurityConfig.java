package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR,
                        AUTHORITY_USER
                )
                .anyRequest().permitAll()
                //TODO: 处理所有异步请求的 CSRF
                        .and().csrf().disable();

        // 权限不够时
        http.exceptionHandling()
                // 登录后权限不够的处理
                .accessDeniedHandler((request, response, e) -> {
                    String xRequestWith = request.getHeader("x-requested-with");
                    if("XMLHttpRequest".equals(xRequestWith)) {
                        // 这是个异步请求
                        response.setContentType("application/plain;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.write(CommunityUtil.getJSONString(403, "权限不足。"));
                    } else {
                        response.sendRedirect(request.getContextPath()+"/denied");
                    }
                })
                // 没登录时的处理
                .authenticationEntryPoint((request, response, e) -> {
                    String xRequestWith = request.getHeader("x-requested-with");
                    if("XMLHttpRequest".equals(xRequestWith)) {
                        // 这是个异步请求
                        response.setContentType("application/plain;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.write(CommunityUtil.getJSONString(403, "你还没有登录。"));
                    } else {
                        response.sendRedirect(request.getContextPath()+"/login");
                    }
                });

        // 覆盖 Security 的 logout 处理,转而执行自己的逻辑
        http.logout().logoutUrl("/securityLogout");
    }
}
