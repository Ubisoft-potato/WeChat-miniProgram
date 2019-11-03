package com.cust.controller;


import com.cust.service.UserSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class UserSearchController {
    private UserSearchService userSearchService;

    private RedisTemplate<Object, Object> redisTemplate;

    @RequestMapping("/loadPhotos")
    public List<Map> showPhotos(HttpServletRequest request) {
        Map<String, String[]> map = request.getParameterMap();
        String thirdSessionKey = (String) map.get("thirdSessionKey")[0];
        //如果reqMap中含有thirdSessionKey进行个性化推荐显示
        System.out.println(thirdSessionKey);
        if (thirdSessionKey.equals("")) {
            return userSearchService.selectAllPhotos(Integer.parseInt((String) (map.get("selectRow")[0])));
        } else {
            System.out.println("asd");
        }
        return null;
    }

    /**
     * 点赞或者取消
     * 参数：photoId
     *
     * @param request
     * @return
     */
    @RequestMapping("/increaseOrDecreaseLike")
    public int increaseOrDecreaseLike(HttpServletRequest request) {
        String photoId = request.getParameter("photoId");
        String likeOrDislike = request.getParameter("likeOrDislike");
        String redisKey = "photoLikeNum" + photoId;
        if (likeOrDislike.equals("1")) {
            if (redisTemplate.hasKey(redisKey)) {
                redisTemplate.opsForValue().set(redisKey, (int) redisTemplate.opsForValue().get(redisKey) + 1);
            }
        }
        if (likeOrDislike.equals("0")) {
            if (redisTemplate.hasKey(redisKey)) {
                redisTemplate.opsForValue().set(redisKey, (int) redisTemplate.opsForValue().get(redisKey) - 1);
            }
        }


        return 0;
    }

    @Autowired
    public UserSearchController(UserSearchService userSearchService, RedisTemplate<Object, Object> redisTemplate) {
        this.userSearchService = userSearchService;
        this.redisTemplate = redisTemplate;
    }
}