package com.newtouch.orderbook.impl;

import com.newtouch.dto.RingBufferRequest;
import com.newtouch.dto.market.L1MarketData;
import com.newtouch.enums.OrderResponseEnum;
import com.newtouch.orderbook.IOrderBook;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderBookImpl implements IOrderBook {

    /**
     * 每个股票都有自己的订单簿 这里定义股票代码
     */
    @NonNull
    private int code;

    @Override
    public OrderResponseEnum newOrder(RingBufferRequest ringBufferRequest) {
        return null;
    }

    @Override
    public OrderResponseEnum cancelOrder(RingBufferRequest ringBufferRequest) {
        return null;
    }

    @Override
    public void fillBuys(int size, L1MarketData data) {

    }

    @Override
    public void fillSells(int size, L1MarketData data) {

    }

    @Override
    public void fillCode(L1MarketData data) {

    }

    @Override
    public int limitBuyBucketSize(int maxSize) {
        return 0;
    }

    @Override
    public int limitSellBucketSize(int maxSize) {
        return 0;
    }
}
