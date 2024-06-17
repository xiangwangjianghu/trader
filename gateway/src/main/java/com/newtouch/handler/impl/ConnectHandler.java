package com.newtouch.handler.impl;

import com.newtouch.handler.IMsgHandler;
import com.newtouch.transport.codec.IBodyCodec;
import com.newtouch.utils.OrderRecordParser;
import io.vertx.core.Handler;
import io.vertx.core.net.NetSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 处理柜台
 */
@Slf4j
@Component
public class ConnectHandler implements Handler<NetSocket> {

    @Autowired
    private IBodyCodec bodyCodec;

    @Autowired
    private IMsgHandler msgHandler;

    @Autowired
    private OrderRecordParser orderRecordParser;

    @Override
    public void handle(NetSocket netSocket) {
        // 1.调用开启連接方法
        msgHandler.onConnect(netSocket);

        // 2.设置消息处理器
        netSocket.handler(orderRecordParser.getRecordParser());
        // 3.异常退出处理器
        netSocket.closeHandler(e -> {
            msgHandler.onDisConnect(netSocket);
        });

        netSocket.exceptionHandler(e -> {
            msgHandler.onException(netSocket, e);
        });
    }
}
