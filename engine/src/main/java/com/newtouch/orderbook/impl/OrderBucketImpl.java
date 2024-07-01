package com.newtouch.orderbook.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.newtouch.dto.MatchEvent;
import com.newtouch.dto.RingBufferRequest;
import com.newtouch.enums.OrderStatusEnum;
import com.newtouch.orderbook.IOrderBucket;
import com.newtouch.orderbook.MatchOrder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

// OrderBucket: 同一檔價格
@Slf4j
@Data
@Component
public class OrderBucketImpl implements IOrderBucket {

    // 同一檔價格
    private BigDecimal price;

    // 剩餘量
    private BigDecimal totalCount = new BigDecimal(0L);

    // 委託列表
    private final LinkedHashMap<Long, MatchOrder> orderMap = new LinkedHashMap<>();

    @Override
    public void add(MatchOrder matchOrder) {
        orderMap.put(matchOrder.getOid(), matchOrder);

        totalCount = totalCount.add(matchOrder.getCount().subtract(matchOrder.getDeal()));
    }

    @Override
    public MatchOrder delete(long oid) {
        MatchOrder matchOrder = orderMap.get(oid);
        if (matchOrder == null) {
            return null;
        }

        orderMap.remove(oid);
        totalCount = totalCount.subtract(matchOrder.getCount().subtract(matchOrder.getDeal()));

        return matchOrder;
    }

    @Override
    public BigDecimal match(BigDecimal countLeft, RingBufferRequest ringBufferRequest, Consumer<MatchOrder> deleteCallback) {

        Iterator<Map.Entry<Long, MatchOrder>> iterator = orderMap.entrySet().iterator();

        // 撮合量
        BigDecimal matchCount = new BigDecimal(0L);
        while (iterator.hasNext() && countLeft.compareTo(BigDecimal.valueOf(0L)) > 0) {
            Map.Entry<Long, MatchOrder> matchOrderEntry = iterator.next();
            MatchOrder matchOrder = matchOrderEntry.getValue();

            // 可以撮合量
            BigDecimal canMatch = countLeft.min(matchOrder.getCount().subtract(matchOrder.getDeal()));
            matchCount = matchCount.add(canMatch);
            countLeft = countLeft.subtract(canMatch);
            totalCount = totalCount.subtract(canMatch);
            matchOrder.setDeal(matchOrder.getDeal().add(canMatch));

            // 完成撮合
            boolean orderMatch = matchOrder.getCount().compareTo(matchOrder.getDeal()) == 0;
            boolean leftMatch = countLeft.compareTo(new BigDecimal(0L)) == 0;

            createMatchEvent(matchOrder, ringBufferRequest, orderMatch, leftMatch, canMatch);

        }

        return matchCount;
    }

    private void createMatchEvent(MatchOrder matchOrder, RingBufferRequest ringBufferRequest, boolean orderMatch, boolean leftMatch, BigDecimal canMatch) {
        long timestamp = System.currentTimeMillis();
        long tid = IdWorker.getId();

        // 两个買賣MatchEvent
        MatchEvent bid = MatchEvent.builder()
                .oid(ringBufferRequest.getOid())
                .price(ringBufferRequest.getPrice())
                .count(canMatch)
                .status(leftMatch ? OrderStatusEnum.TRADE_ED : OrderStatusEnum.PART_TRADE)
                .tid(tid)
                .timestamp(timestamp)
                .build();
        MatchEvent offer = MatchEvent.builder()
                .oid(matchOrder.getOid())
                .price(matchOrder.getPrice())
                .count(canMatch)
                .status(orderMatch ? OrderStatusEnum.TRADE_ED : OrderStatusEnum.PART_TRADE)
                .tid(tid)
                .timestamp(timestamp)
                .build();

        ringBufferRequest.getMatchEventList().add(bid);
        ringBufferRequest.getMatchEventList().add(offer);
    }

    @Override
    public BigDecimal getPrice() {
        return this.price;
    }

    @Override
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public BigDecimal getTotalCount() {
        return totalCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderBucketImpl that = (OrderBucketImpl) o;
        return new EqualsBuilder()
                .append(this.price, that.price)
                .append(this.orderMap, that.orderMap)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(price)
                .append(orderMap)
                .toHashCode();
    }
}
