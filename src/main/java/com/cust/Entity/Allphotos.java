package com.cust.Entity;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Allphotos {
    private String photoid;

    private String ownerid;

    private String instruction;

    private String location;

    private String photourl;

    private Integer likenum;

    private String createTime;

    private String categories;

}