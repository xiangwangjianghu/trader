package com.newtouch.orderbook;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderBucketType {
    GUDY((byte) 0);

    private final byte code;
}
