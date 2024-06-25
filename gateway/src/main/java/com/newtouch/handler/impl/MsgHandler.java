package com.newtouch.handler.impl;

import com.newtouch.dto.order.CommonMsg;
import com.newtouch.dto.order.OrderRequest;
import com.newtouch.handler.IMsgHandler;
import com.newtouch.transport.codec.IBodyCodec;
import com.newtouch.utils.OrderRequestQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MsgHandler implements IMsgHandler {

    @Autowired
    private IBodyCodec bodyCodec;

    @Autowired
    private OrderRequestQueue orderRequestQueue;

    @Override
    public void onCounterData(CommonMsg msg) {
        OrderRequest orderRequest;

        try {
            orderRequest = bodyCodec.deserialize(msg.getBody(), OrderRequest.class);
            log.info("接受訂單請求: {}", orderRequest);

            // 写入委托订单缓存队列
            if (!orderRequestQueue.offer(orderRequest)) {
                log.info("進入網關隊列失敗: {}", orderRequest);
            }
        } catch (Exception e) {
            log.error("解碼訂單請求出錯!", e);
        }
    }
}
