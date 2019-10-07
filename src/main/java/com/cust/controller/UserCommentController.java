package com.cust.controller;

import com.cust.Entity.Commentreply;
import com.cust.Entity.Photocomment;
import com.cust.Utils.Token;
import com.cust.service.UserCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class UserCommentController {


    private RedisTemplate<Object, Object> redisTemplate;

    private UserCommentService userCommentService;


    /**
     * 获取redis中缓存的主评论
     *
     * @param request 前端携带photoId
     * @return
     */
    @RequestMapping("/getMainPhotoComment")
    public List getMainPhotoComment(HttpServletRequest request) {

        String photoId = String.valueOf(request.getParameter("photoId"));
        String md5PhotoId = Token.getMD5(photoId);
        String commentIndex = String.valueOf(request.getParameter("commentIndex"));
        if (Integer.parseInt(commentIndex) >= redisTemplate.opsForList().size(md5PhotoId) && Integer.parseInt(commentIndex) != 0) {
            return new ArrayList();
        }
        List mainComments = redisTemplate.opsForList().range(md5PhotoId, Integer.parseInt(commentIndex), Integer.parseInt(commentIndex) + 9);
        if (mainComments == null || mainComments.size() == 0) {
            mainComments = userCommentService.getMainComment(photoId);
        }
        return mainComments;
    }

    /**
     * 评论页面下拉刷新（直接从数据库中获取）
     *
     * @param request
     * @return
     */
    @RequestMapping("/refreshComments")
    public List refreshComments(HttpServletRequest request) {
        String photoId = String.valueOf(request.getParameter("photoId"));
        return userCommentService.getMainComment(photoId);
    }


    /**
     * 发布主评论
     * 前端携带主评论photoId，thirdSessionKey,content,nickName,avatarUrl
     * 返回值：
     * 1：发布成功
     * -1：发布失败
     * -2：未登录，前端跳转到登陆界面
     *
     * @param request
     * @return
     */
    @RequestMapping("/publishMainComment")
    public int publishMainComment(HttpServletRequest request) {
        String thirdSessionKey = request.getParameter("thirdSessionKey");
        //fromId:获取发布主评论人的userId
        String fromId = (String) redisTemplate.opsForValue().get(thirdSessionKey);
//        System.out.println(thirdSessionKey + ":" + fromId);
        String content = request.getParameter("content");
//        System.out.println(content);
        String photoId = request.getParameter("photoId");
        String md5PhotoId = Token.getMD5(photoId);

//        System.out.println(photoId);

        String fromURL = request.getParameter("fromURL");
        String fromname = request.getParameter("fromname");

        if (fromId == null) {
            return -2;
        }
        Photocomment photocomment = new Photocomment();
        Map<String, String> redisMianComment = new HashMap<>();
        photocomment.setContent(content);
        redisMianComment.put("content", content);
        photocomment.setFromid(fromId);
        redisMianComment.put("fromid", fromId);
        photocomment.setPhotoId(photoId);
        photocomment.setFromname(fromname);
        redisMianComment.put("fromname", fromname);
        photocomment.setFromurl(fromURL);
        redisMianComment.put("fromURL", fromURL);
        photocomment.setId(Token.createNewUserId());
        redisMianComment.put("id", photocomment.getId());
        photocomment.setCreatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        redisMianComment.put("createTime", photocomment.getCreatetime());
        if (!userCommentService.publishMainComment(photocomment)) {
            return -1;
        }
        //同步redis中的评论
        redisTemplate.opsForList().rightPush(md5PhotoId, redisMianComment);
        redisTemplate.expire(md5PhotoId, 1, TimeUnit.DAYS);
        return 1;
    }


    /**
     * e
     * 获取子评论
     * 前端携带主评论id
     *
     * @param request
     * @return
     */
    @RequestMapping("/getSonComments")
    public List getSonComments(HttpServletRequest request) {
        String commentId = request.getParameter("commentId");
        String commentIndex = String.valueOf(request.getParameter("commentIndex"));
        if (Integer.parseInt(commentIndex) >= redisTemplate.opsForList().size(commentId) && Integer.parseInt(commentIndex) != 0) {
            return new ArrayList();
        }
        List sonComments = redisTemplate.opsForList().range(commentId, Integer.parseInt(commentIndex), Integer.parseInt(commentIndex) + 9);
        if (sonComments == null || sonComments.size() == 0) {
            sonComments = userCommentService.getSonComment(commentId);
        }
        return sonComments;
    }

    /**
     * 刷新子评论页面
     *
     * @param request
     * @return
     */
    @RequestMapping("/refreshSonComments")
    public List refreshSonComments(HttpServletRequest request) {
        String commentId = request.getParameter("commentId");
        return userCommentService.getSonComment(commentId);
    }

    /**
     * 子评论发布
     * 前端携带主评论commentId，thirdSessionKey,content,nickName,avatarUrl
     * 返回值：
     * 1：发布成功
     * -1：发布失败
     * -2：未登录，前端跳转到登陆界面
     *
     * @param request
     * @return
     */
    @RequestMapping("/commentsReply")
    public int commentsReply(HttpServletRequest request) {
        String thirdSessionKey = request.getParameter("thirdSessionKey");
        String fromId = (String) redisTemplate.opsForValue().get(thirdSessionKey);
        String content = request.getParameter("content");
        String commentId = request.getParameter("commentId");
        String fromURL = request.getParameter("fromURL");
        String fromname = request.getParameter("fromname");

        if (fromId == null) {
            return -2;
        }
        Commentreply commentreply = new Commentreply();
        Map<String, String> redisSonComment = new HashMap<>();
        commentreply.setCommentid(commentId);
        commentreply.setContent(content);
        redisSonComment.put("content", content);
        commentreply.setCreatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        redisSonComment.put("createTime", commentreply.getCreatetime());
        commentreply.setFromid(fromId);
        redisSonComment.put("fromid", fromId);
        commentreply.setFromname(fromname);
        redisSonComment.put("fromname", fromname);
        commentreply.setFromurl(fromURL);
        redisSonComment.put("fromURL", fromURL);

        if (!userCommentService.publishSonComment(commentreply)) {
            return -1;
        }
        redisTemplate.opsForList().rightPush(commentId, redisSonComment);
        redisTemplate.expire(commentId, 1, TimeUnit.DAYS);
        return 1;
    }

    /**
     * 评论后刷新当前页面
     *
     * @param request
     * @return
     */
    @RequestMapping("/afterPublishRefresh")
    public List afterPublishRefresh(HttpServletRequest request) {
        String photoId = request.getParameter("photoId");
        String commentId = request.getParameter("commentId");
        System.out.println(commentId);
        System.out.println(photoId);
        if (photoId != null && !photoId.equals("")) {
            System.out.println("photoId");
            String md5PhotoId = Token.getMD5(photoId);
            return redisTemplate.opsForList().range(md5PhotoId, 0, -1);
        }
        if (commentId != null && !commentId.equals("")) {
            System.out.println("commentId");
            List publishedSonComment = redisTemplate.opsForList().range(commentId, 0, -1);
            System.out.println(publishedSonComment);
            return publishedSonComment;
        }
        return new ArrayList();
    }

    @Autowired
    public UserCommentController(RedisTemplate<Object, Object> redisTemplate,
                                 UserCommentService userCommentService) {
        this.redisTemplate = redisTemplate;
        this.userCommentService = userCommentService;
    }
}
