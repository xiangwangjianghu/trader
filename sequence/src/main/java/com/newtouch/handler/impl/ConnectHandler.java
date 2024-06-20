package com.newtouch.handler.impl;

import com.newtouch.dto.CommonMsg;
import com.newtouch.handler.IMsgHandler;
import com.newtouch.transport.checksum.ICheckSum;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 处理柜台
 */
@Slf4j
@Component
public class ConnectHandler implements Handler<NetSocket> {
    @Autowired
    private ICheckSum checkSum;

    @Autowired
    private IMsgHandler msgHandler;

    @Value("${counter.gateway-id}")
    private Short gatewayId;

    // 包头[ 包体长度 int + 校验和 byte + src short+ dst short + 消息类型 short + 消息状态 byte + 包编号 long ]
    private static final int PACKET_HEADER_LENGTH = 4 + 1 + 2 + 2 + 2 + 1 + 8;

    @Override
    public void handle(NetSocket netSocket) {
        // 1.调用开启連接方法
        msgHandler.onConnect(netSocket);

        // 2.设置消息处理器
        RecordParser recordParser = RecordParser.newFixed(PACKET_HEADER_LENGTH);

        recordParser.setOutput(new Handler<>() {
            //    包头[ 包体长度 int + 校验和 byte + src short+ dst short + 消息类型 short + 消息状态 byte + 包编号 long ]
            int bodyLength = -1;
            byte checksum = -1;
            short msgSrc = -1;
            short msgDst = -1;
            short msgType = -1;
            byte status = -1;
            long packetNo = -1;

            @Override
            public void handle(Buffer buffer) {
                if (bodyLength == -1) {
                    // 读到包头
                    bodyLength = buffer.getInt(0);
                    checksum = buffer.getByte(4);
                    msgSrc = buffer.getShort(5);
                    msgDst = buffer.getShort(7);
                    msgType = buffer.getShort(9);
                    status = buffer.getByte(11);
                    packetNo = buffer.getLong(12);

                    // 读满这个长度才回调 当前handle方法
                    recordParser.fixedSizeMode(bodyLength);
                } else {
                    // 如果size != -1, 说明已经接受到长度信息了，接下来的数据就是protobuf可识别的字节数组
                    byte[] bodyBytes = buffer.getBytes();

                    if (checksum != checkSum.getChecksum(bodyBytes)) {
                        log.error("客戶端傳輸消息校驗和非法!");
                        return;
                    }

                    // 判断目标id和柜台是否一致
                    if (msgDst != gatewayId) {
                        log.error("網關目的地: {} 和 預設網關: {} 不匹配", msgDst, gatewayId);
                        return;
                    }

                    CommonMsg commonMsg = CommonMsg.builder()
                            .bodyLength(bodyBytes.length)
                            .checksum(checksum)
                            .msgSrc(msgSrc)
                            .msgDst(msgDst)
                            .msgType(msgType)
                            .status(status)
                            .msgNo(packetNo)
                            .body(bodyBytes)
                            .timestamp(System.currentTimeMillis())
                            .build();

                    // 调用上报柜台方法
                    msgHandler.onCounterData(commonMsg);

                    // 重置
                    bodyLength = -1;
                    checksum = -1;
                    msgSrc = -1;
                    msgDst = -1;
                    msgType = -1;
                    status = -1;
                    packetNo = -1;
                    recordParser.fixedSizeMode(PACKET_HEADER_LENGTH);
                }
            }
        });

        netSocket.handler(recordParser);
        // 3.异常退出处理器
        netSocket.closeHandler(e -> {
            msgHandler.onDisConnect(netSocket);
        });

        netSocket.exceptionHandler(e -> {
            msgHandler.onException(netSocket, e);
        });
    }
}
