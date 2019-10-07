package com.cust.dao;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AllphotosMapper {

//    @Insert("insert into allphotos(photoId,ownerId,instruction,location,photoURL,createTime,categories)values(#{photoId},#{ownerId},#{instruction},#{location},#{photoURL},#{createTime},{categories})")
    int firstInsert(Map record);

//    @Update("update allphotos set photoURL=CONCAT(photoURL,#{photoURL}) where photoId=#{photoId}")
    int nextInsert(Map record);

//    @Select("select allphotos.*,user.avatarURL,user.nickname from allphotos left join user on user.id=allphotos.ownerId order by allphotos.likeNum desc,allphotos.createTime desc LIMIT #{selectRow},14")
    List<Map> randomSelect(int selectRow);

    String getPhotoUrlByphotoId(String photoId);

    List<Map> getPicListByUserID(Map map);

    int delPicById(String photoId);


}