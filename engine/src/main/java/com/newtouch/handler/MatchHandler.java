package com.newtouch.handler;

import com.lmax.disruptor.EventHandler;
import com.newtouch.dto.RingBufferRequest;
import com.newtouch.enums.OrderResponseEnum;
import com.newtouch.orderbook.IOrderBook;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.springframework.stereotype.Component;

import static com.newtouch.enums.OrderRequestTypeConstants.*;
import static com.newtouch.enums.OrderResponseEnum.SUCCESS;

// 前置风控处理器
@Slf4j
@Component
public class MatchHandler implements EventHandler<RingBufferRequest> {

    private IntObjectHashMap<IOrderBook> orderBookMap;

    public MatchHandler() {
    }

    public MatchHandler(IntObjectHashMap<IOrderBook> orderBookMap) {
        this.orderBookMap = orderBookMap;
    }

    @Override
    public void onEvent(RingBufferRequest ringBufferRequest, long sequence, boolean endOfBatch) throws Exception {
        // 风控未通过
        if (ringBufferRequest.getOrderResponseEnum().getCode() < 0) {
            return;
        }

        // 进行撮合
        ringBufferRequest.setOrderResponseEnum(match(ringBufferRequest));
    }

    private OrderResponseEnum match(RingBufferRequest ringBufferRequest) {
        switch (ringBufferRequest.getOrderRequestType()) {
            case NEW_ORDER:
                return orderBookMap.get(ringBufferRequest.getCode()).newOrder(ringBufferRequest);
            case CANCEL_ORDER:
                return orderBookMap.get(ringBufferRequest.getCode()).cancelOrder(ringBufferRequest);
            case HQ_PUB:
                orderBookMap.forEachKeyValue((code, orderBook) -> {
                    ringBufferRequest.getMarketDataMap().put(code, orderBook.getL1MarketData());
                });
            default:
                return SUCCESS;
        }
    }
}
