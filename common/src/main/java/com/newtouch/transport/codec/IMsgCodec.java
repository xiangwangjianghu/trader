package com.newtouch.transport.codec;

import com.newtouch.dto.order.CommonMsg;
import io.vertx.core.buffer.Buffer;

public interface IMsgCodec {

    //TCP <--> CommonMsg
    Buffer encodeToBuffer(CommonMsg msg);

    CommonMsg decodeFromBuffer(Buffer buffer);
}
