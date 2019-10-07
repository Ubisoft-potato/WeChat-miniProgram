package com.cust.dao;

import com.cust.Entity.Commentreply;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentreplyMapper {

    int insertSonComment(Commentreply commentreply);

    List<Map> selectSonComments(String commnetId);
}