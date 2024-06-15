package com.newtouch.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IdUtil {

    public long combineOid(long high, long low) {
        return (high << 32 & 0xFFFFFFFF00000000L) | (low & 0xFFFFFFFFL);
    }

    public long[] separateOid(long val) {
        long[] res = new long[2];
        res[1] = (0xFFFFFFFFL & val);                         //低位
        res[0] = ((0xFFFFFFFF00000000L & val) >> 32);         //高位

        return res;
    }
}
