package com.cust.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class User {
    private String id;

    private String nickname;

    private Integer gender;

    private String province;

    private String city;

    private String country;

    private String openid;

    private String username;

    private String password;

    private String avatarURL;
}