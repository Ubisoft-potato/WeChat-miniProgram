package com.cust.dao;

import com.cust.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface UserMapper {
//    @Select("select * from user where openid=#{openid}")
    User selectUser(String openid);

//    @Insert("insert into user (id,nickname,gender,province,city,country,openid,avatarURL) values(#{id},#{nickname},#{gender},#{province},#{city},#{country},#{openid},#{avatarURL})")
    int insertUserInfo(User user);

    int changeNickName(Map userChange);

}