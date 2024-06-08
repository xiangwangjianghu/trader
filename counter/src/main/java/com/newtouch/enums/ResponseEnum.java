package com.newtouch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseEnum {

    SUCCESS(200, "成功"),

    ;

    int code;

    String msg;
}
