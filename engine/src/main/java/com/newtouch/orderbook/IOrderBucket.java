package com.newtouch.orderbook;

import com.newtouch.dto.RingBufferRequest;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public interface IOrderBucket extends Comparable<IOrderBucket> {

    // 撮合單號
    AtomicLong tid = new AtomicLong(0L);

    // 新增订单
    void add(MatchOrder matchOrder);

    // 移除订单
    MatchOrder delete(long oid);

    BigDecimal match(BigDecimal countLeft, RingBufferRequest ringBufferRequest, Consumer<MatchOrder> deleteCallback);

    // 價格發佈
    BigDecimal getPrice();

    // 設置價格
    void setPrice(BigDecimal price);

    // 当前桶累计委托量
    BigDecimal getTotalCount();

    @Override
    default int compareTo(IOrderBucket other) {
        return this.getPrice().compareTo(other.getPrice());
    }
}
