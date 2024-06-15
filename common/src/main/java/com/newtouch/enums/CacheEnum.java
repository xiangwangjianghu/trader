package com.newtouch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheEnum {

    CAPTCHA("captcha:"),

    USER("user:"),

    ORDER("order:"),

    TRADE("trade:"),

    POSITION("position:"),
    ;

    private String type;
}
