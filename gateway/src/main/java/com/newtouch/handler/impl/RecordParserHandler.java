package com.newtouch.handler.impl;

import com.newtouch.handler.IMsgHandler;
import com.newtouch.transport.codec.IBodyCodec;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 固定长度解析器
 */
@Slf4j
@Component
public class RecordParserHandler implements Handler<Buffer> {

    // 包头[ 包体长度 int + 校验和 byte + src short+ dst short + 消息类型 short + 消息状态 byte + 包编号 long ]
    private static final int PACKET_HEADER_LENGTH = 4 + 1 + 2 + 2 + 2 + 1 + 8;

    @Override
    public void handle(Buffer event) {

    }
}
