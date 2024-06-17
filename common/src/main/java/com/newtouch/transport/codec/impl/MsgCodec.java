package com.newtouch.transport.codec.impl;

import com.newtouch.dto.CommonMsg;
import com.newtouch.transport.codec.IMsgCodec;
import io.vertx.core.buffer.Buffer;

public class MsgCodec implements IMsgCodec {
    @Override
    public Buffer encodeToBuffer(CommonMsg msg) {
        return Buffer.buffer().appendInt(msg.getBodyLength())
                .appendByte(msg.getChecksum())
                .appendShort(msg.getMsgSrc())
                .appendShort(msg.getMsgDst())
                .appendShort(msg.getMsgType())
                .appendByte(msg.getStatus())
                .appendLong(msg.getMsgNo())
                .appendBytes(msg.getBody());
    }

    @Override
    public CommonMsg decodeFromBuffer(Buffer buffer) {

        int bodyLength = buffer.getInt(0);
        byte checksum = buffer.getByte(4);
        short msgSrc = buffer.getShort(5);
        short msgDst = buffer.getShort(7);
        short msgType = buffer.getShort(9);
        byte status = buffer.getByte(11);
        long packetNo = buffer.getLong(12);
        byte[] bodyBytes = buffer.getBytes(20, 20 + bodyLength);

        return CommonMsg.builder()
                .bodyLength(bodyBytes.length)
                .checksum(checksum)
                .msgSrc(msgSrc)
                .msgDst(msgDst)
                .msgType(msgType)
                .status(status)
                .msgNo(packetNo)
                .body(bodyBytes)
                .build();
    }
}
