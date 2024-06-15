package com.newtouch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderDirectionEnum {

    BUY(0),
    SELL(1),
    ;

    private final Integer direction;
}
