package com.newtouch.dto.market;

import com.newtouch.enums.OrderStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

// 發佈到總線櫃檯
@Builder
@Data
public class MatchData {
    public long timestamp;

    public long oid;

    public OrderStatusEnum status;

    public long tid;

    //撤单数量 成交数量
    public BigDecimal count;

    public BigDecimal price;
}
