package com.newtouch.enums;

import lombok.Getter;

@Getter
public enum CacheEnum {

    CAPTCHA("captcha:"),

    ACCOUNT("account:"),

    ORDER("order:"),

    TRADE("trade:"),

    POSI("posi:"),
    ;

    private String type;

    CacheEnum(String type) {
        this.type = type;
    }
}
