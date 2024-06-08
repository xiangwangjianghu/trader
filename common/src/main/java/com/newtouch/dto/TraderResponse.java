package com.newtouch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class TraderResponse<T> {

    int code;

    String msg;

    T data;
}
