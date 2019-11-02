package com.cust.service;

import com.cust.entity.Commentreply;
import com.cust.entity.Photocomment;
import com.cust.utils.Token;
import com.cust.dao.CommentreplyMapper;
import com.cust.dao.PhotocommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserCommentService {

    private PhotocommentMapper photocommentMapper;

    private CommentreplyMapper commentreplyMapper;

    private RedisTemplate<Object, Object> redisTemplate;

    public List<Map> getMainComment(String photoId) {
        String md5PhotoId = Token.getMD5(photoId);
        List<Map> result = photocommentMapper.selectByPhotoId(photoId);
        if (result != null && result.size() != 0) {
            if (redisTemplate.opsForList().size(md5PhotoId) < result.size()) {
                if (redisTemplate.hasKey(md5PhotoId)) {
                    redisTemplate.delete(md5PhotoId);
                }
                for (Map comment : result) {
                    redisTemplate.opsForList().rightPush(md5PhotoId, comment);
                }
                redisTemplate.expire(md5PhotoId, 1, TimeUnit.DAYS);
            }

        }
        return result;
    }

    public List<Map> getSonComment(String commentId) {
        List<Map> result = commentreplyMapper.selectSonComments(commentId);
        if (result != null && result.size() != 0) {
            if (redisTemplate.opsForList().size(commentId) < result.size()) {
                if (redisTemplate.hasKey(commentId)) {
                    redisTemplate.delete(commentId);
                }
                for (Map comment : result) {
                    redisTemplate.opsForList().rightPush(commentId, comment);
                }
                redisTemplate.expire(commentId, 1, TimeUnit.DAYS);
            }

        }
        return result;
    }

    public boolean publishMainComment(Photocomment photocomment) {

        return photocommentMapper.insertMainComment(photocomment) > 0;
    }

    public boolean publishSonComment(Commentreply commentreply) {

        return commentreplyMapper.insertSonComment(commentreply) > 0;
    }

    @Autowired
    public UserCommentService(PhotocommentMapper photocommentMapper,
                              CommentreplyMapper commentreplyMapper,
                              RedisTemplate<Object, Object> redisTemplate) {
        this.photocommentMapper = photocommentMapper;
        this.commentreplyMapper = commentreplyMapper;
        this.redisTemplate = redisTemplate;
    }
}
