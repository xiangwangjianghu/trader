package com.newtouch.orderbook.impl;

import com.newtouch.dto.RingBufferRequest;
import com.newtouch.orderbook.IOrderBucket;
import com.newtouch.orderbook.MatchOrder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Objects;
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
    public long match(BigDecimal countLeft, RingBufferRequest ringBufferRequest, Consumer<MatchOrder> deleteCallback) {
        return 0;
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
