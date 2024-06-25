package com.newtouch.handler;

import com.lmax.disruptor.EventHandler;
import com.newtouch.dto.RingBufferRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.springframework.stereotype.Component;

import static com.newtouch.enums.OrderRequestTypeConstants.*;
import static com.newtouch.enums.OrderResponseEnum.*;

// 前置风控处理器
@Slf4j
@Component
public class RiskHandler implements EventHandler<RingBufferRequest> {

    private MutableLongSet uidSet;

    private MutableIntSet codeSet;

    public RiskHandler() {
    }

    public RiskHandler(MutableLongSet uidSet, MutableIntSet codeSet) {
        this.uidSet = uidSet;
        this.codeSet = codeSet;
    }

    @Override
    public void onEvent(RingBufferRequest ringBufferRequest, long sequence, boolean endOfBatch) {
        Integer orderRequestType = ringBufferRequest.getOrderRequestType();

        if (orderRequestType == HQ_PUB) {
            return;
        }

        if (orderRequestType == NEW_ORDER || orderRequestType == CANCEL_ORDER) {
            if (!uidSet.contains(ringBufferRequest.getUid())) {
                log.error("uid[{}] 不存在!", ringBufferRequest.getUid());

                ringBufferRequest.setOrderResponseEnum(RISK_INVALID_USER);
                return;
            }

            if (!codeSet.contains(ringBufferRequest.getCode())) {
                log.error("股票代碼[{}] 不存在!", ringBufferRequest.getCode());

                ringBufferRequest.setOrderResponseEnum(RISK_INVALID_CODE);
            }
        }
    }
}
