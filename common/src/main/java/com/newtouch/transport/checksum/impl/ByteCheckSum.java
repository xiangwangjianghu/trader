package com.newtouch.transport.checksum.impl;

import com.newtouch.transport.checksum.ICheckSum;
import org.springframework.stereotype.Component;

@Component
public class ByteCheckSum implements ICheckSum {
    @Override
    public byte getChecksum(byte[] data) {
        byte sum = 0;

        for (byte d : data) {
            sum ^= d;
        }
        return sum;
    }
}
