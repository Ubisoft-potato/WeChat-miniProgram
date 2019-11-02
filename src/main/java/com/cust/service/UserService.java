package com.cust.service;

import com.cust.entity.User;
import com.cust.dao.AllphotosMapper;
import com.cust.dao.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {
    private UserMapper userMapper;

    private AllphotosMapper allphotosMapper;
    private RedisTemplate<Object, Object> redisTemplate;

    public String selectUserOpenId(String openid) {
        User userInfo = userMapper.selectUser(openid);
        if (userInfo != null) {
            return userInfo.getId();
        } else {
            return null;
        }
    }

    public boolean insertUserInfo(User userInfo) {
        return userMapper.insertUserInfo(userInfo) > 0;
    }

    public boolean firstSave(Map<String, String> saveMap) {
        int i = allphotosMapper.firstInsert(saveMap);
        //System.out.println(i);
        return i > 0;
    }

    public boolean nextSave(Map<String, String> saveMap) {
        return allphotosMapper.nextInsert(saveMap) > 0;
    }

    public boolean changeNickName(Map userChange) {
        return userMapper.changeNickName(userChange) > 0;
    }


    @Autowired
    public UserService(UserMapper userMapper,
                       AllphotosMapper allphotosMapper,
                       RedisTemplate<Object, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.allphotosMapper = allphotosMapper;
        this.redisTemplate = redisTemplate;
    }
}
