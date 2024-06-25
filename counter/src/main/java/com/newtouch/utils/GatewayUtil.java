package com.newtouch.utils;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.newtouch.dto.order.CommonMsg;
import com.newtouch.dto.order.OrderRequest;
import com.newtouch.enums.MsgConstants;
import com.newtouch.transport.checksum.ICheckSum;
import com.newtouch.transport.codec.IBodyCodec;
import com.newtouch.transport.codec.IMsgCodec;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GatewayUtil {

    @Autowired
    private ICheckSum checkSum;

    @Autowired
    private IBodyCodec bodyCodec;

    @Autowired
    private IMsgCodec msgCodec;

    @Autowired
    private TCPUtil tcpUtil;

    @Value("${counter.id}")
    private Short counterId;

    @Value("${counter.gateway-id}")
    private Short gatewayId;

    @Value("${counter.gateway-ip}")
    private String senderIp;

    @Value("${counter.gateway-port}")
    private int senderPort;

    @PostConstruct
    private void init() {
        //初始化 tcp目标发送工具 （发送到网关 ）
        tcpUtil.senderIp = senderIp;
        tcpUtil.senderPort = senderPort;

        tcpUtil.startup();
    }

    // 发送订单
    public void sendOrder(OrderRequest orderRequest) {
        byte[] data;

        try {
            data = bodyCodec.serialize(orderRequest);
        } catch (Exception e) {
            log.error("序列化orderRequest出錯:{}", JSONObject.toJSONString(orderRequest), e);
            return;
        }

        CommonMsg commonMsg = CommonMsg.builder()
                .bodyLength(data.length)
                .checksum(checkSum.getChecksum(data))
                .msgSrc(counterId)
                .msgDst(gatewayId)
                .msgType(MsgConstants.COUNTER_NEW_ORDER)
                .status(MsgConstants.NORMAL)
                .msgNo(IdWorker.getId())
                .body(data)
                .timestamp(System.currentTimeMillis())
                .build();

        // 发送序列化后的消息
        tcpUtil.send(msgCodec.encodeToBuffer(commonMsg));
    }
}
