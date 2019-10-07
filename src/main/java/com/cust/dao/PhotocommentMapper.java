package com.cust.dao;

import com.cust.Entity.Photocomment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PhotocommentMapper {

    List<Map> selectByPhotoId(String photoId);

    int insertMainComment(Photocomment photocomment);
}