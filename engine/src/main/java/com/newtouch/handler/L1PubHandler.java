package com.newtouch.handler;

import com.lmax.disruptor.EventHandler;
import com.newtouch.dto.RingBufferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 前置风控处理器
@Slf4j
@Component
public class L1PubHandler implements EventHandler<RingBufferRequest> {
    // 发布时间间隔
    public static final int HQ_PUB_RATE = 1000;

    @Override
    public void onEvent(RingBufferRequest ringBufferRequest, long sequence, boolean endOfBatch) throws Exception {

    }
}
