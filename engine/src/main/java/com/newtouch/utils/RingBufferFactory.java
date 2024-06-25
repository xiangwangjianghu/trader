package com.newtouch.utils;

import com.google.common.collect.Lists;
import com.lmax.disruptor.EventFactory;
import com.newtouch.dto.RingBufferRequest;
import com.newtouch.enums.OrderResponseEnum;

public class RingBufferFactory implements EventFactory<RingBufferRequest> {
    @Override
    public RingBufferRequest newInstance() {
        return RingBufferRequest.builder()
                .orderResponseEnum(OrderResponseEnum.SUCCESS)
                .matchEventList(Lists.newArrayList())
                .build();
    }
}
