package com.cust.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Commentreply {
    private Integer id;

    private String commentid;

    private String fromid;

    private String fromname;

    private String content;

    private String createtime;

    private String fromurl;
}