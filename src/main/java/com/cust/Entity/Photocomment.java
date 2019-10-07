package com.cust.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Photocomment {
    private String id;

    private String photoId;

    private String fromid;

    private String fromname;

    private String fromurl;

    private String content;

    private String createtime;

}