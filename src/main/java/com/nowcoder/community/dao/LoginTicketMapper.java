package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    // 登录行为是围绕Ticket进行的，所以查询是以Ticket为条件
    LoginTicket selectByTicket(String ticket);

    @Update({
            "update login_ticket set status=#{status} ",
            "where ticket=#{ticket}"
    })
    // 退出登录-》修改状态
    int updateStatus(String ticket, int status);

}
