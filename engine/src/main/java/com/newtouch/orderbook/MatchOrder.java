package com.newtouch.orderbook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public final class MatchOrder {

    /**
     * 用户ID
     */
    private long uid;

    /**
     * 代码
     */
    private int code;

    /**
     * 方向
     */
    private Integer direction;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 委托量
     */
    private BigDecimal count;

    /**
     * 已成交量
     */
    private BigDecimal deal;

    /**
     * 委托编号
     */
    private long oid;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 内部排序顺序
     */
    private long innerOid;

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(uid)
                .append(code)
                .append(direction)
                .append(price)
                .append(count)
                .append(deal)
                .append(oid)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MatchOrder matchOrder = (MatchOrder) o;

        return new EqualsBuilder()
                .append(uid, matchOrder.uid)
                .append(code, matchOrder.code)
                .append(direction, matchOrder.direction)
                .append(price, matchOrder.price)
                .append(count, matchOrder.count)
                .append(deal, matchOrder.deal)
                .append(oid, matchOrder.oid)
//                .append(timestamp, matchOrder.timestamp)
                .isEquals();
    }
}
