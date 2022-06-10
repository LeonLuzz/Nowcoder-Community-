package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated // 已经使用redis优化，不再推荐使用！
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(id, user_id, ticket, status, expired) " +
            "values(#{id}, #{user_id}, #{ticket}, #{status}, #{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int InsertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select * from login_ticket where ticket = #{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "update login_ticket set status = #{status} where ticket = #{ticket}"
    })
    int updateStatus(String ticket, int status);

}
