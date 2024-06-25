package com.newtouch.orderbook;

import com.newtouch.dto.RingBufferRequest;
import com.newtouch.dto.market.L1MarketData;
import com.newtouch.enums.OrderResponseEnum;

import static com.newtouch.dto.market.L1MarketData.L1_SIZE;

public interface IOrderBook {

    // 委託訂單
    OrderResponseEnum newOrder(RingBufferRequest ringBufferRequest);

    // 取消訂單
    OrderResponseEnum cancelOrder(RingBufferRequest ringBufferRequest);

    // 查询行情
    default L1MarketData getL1MarketData() {
        final int buySize = limitBuyBucketSize(L1_SIZE);
        final int sellSize = limitSellBucketSize(L1_SIZE);
        L1MarketData l1MarketData = new L1MarketData(buySize, sellSize);

        fillBuys(buySize, l1MarketData);
        fillSells(buySize, l1MarketData);
        fillCode(l1MarketData);

        l1MarketData.setTimestamp(System.currentTimeMillis());
        return l1MarketData;
    }

    void fillBuys(int size, L1MarketData data);

    void fillSells(int size, L1MarketData data);

    void fillCode(L1MarketData data);

    int limitBuyBucketSize(int maxSize);

    int limitSellBucketSize(int maxSize);
}
