package com.newtouch.utils;

import org.springframework.stereotype.Component;

@Component
public class IdUtil {

    public Long combineOid(long high, long low) {
        return (high << 32 & 0xFFFFFFFF00000000L) | (low & 0xFFFFFFFFL);
    }

    public Long[] separateOid(long val) {
        Long[] res = new Long[2];
        res[1] = (0xFFFFFFFFL & val);                         //低位
        res[0] = ((0xFFFFFFFF00000000L & val) >> 32);         //高位

        return res;
    }
}
