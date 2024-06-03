package com.newtouch.dto;

import lombok.Data;


@Data
public class TraderResponse<T> {

    private int code;

    private String msg;


    T data;
}
