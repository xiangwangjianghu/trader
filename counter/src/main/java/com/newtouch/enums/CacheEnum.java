package com.newtouch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheEnum {

    CAPTCHA("captcha:"),

    ACCOUNT("account:"),

    ORDER("order:"),

    TRADE("trade:"),

    POSI("posi:"),
    ;

    private String type;
}
