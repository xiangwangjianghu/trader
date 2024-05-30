package com.newtouch.entity;

import lombok.Data;

@Data
public class Account {

    private int id;


    private long uid;


    private String lastLoginDate;

    private String lastLoginTime;

    private String token;
}
