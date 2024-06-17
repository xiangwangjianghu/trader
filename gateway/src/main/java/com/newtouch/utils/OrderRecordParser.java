package com.newtouch.utils;

import com.newtouch.handler.impl.RecordParserHandler;
import io.vertx.core.parsetools.RecordParser;
import org.springframework.stereotype.Component;

/**
 * 固定长度解析器
 */
@Component
public class OrderRecordParser {

    // 包头[ 包体长度 int + 校验和 byte + src short+ dst short + 消息类型 short + 消息状态 byte + 包编号 long ]
    private static final int PACKET_HEADER_LENGTH = 4 + 1 + 2 + 2 + 2 + 1 + 8;

    public RecordParser getRecordParser() {
        RecordParser recordParser = RecordParser.newFixed(PACKET_HEADER_LENGTH);

        recordParser.setOutput(new RecordParserHandler());

        return recordParser;
    }
}
