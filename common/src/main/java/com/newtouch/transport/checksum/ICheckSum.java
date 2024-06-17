package com.newtouch.transport.checksum;

// 校验和
public interface ICheckSum {

    byte getChecksum(byte[] data);
}
