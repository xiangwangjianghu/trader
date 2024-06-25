package com.newtouch.dto;

import com.newtouch.enums.OrderStatusEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

// 撮合數據結構
@Data
@Slf4j
public class MatchEvent {
    public long timestamp;

    public long oid;

    public OrderStatusEnum status = OrderStatusEnum.NOT_SET;

    public long tid;

    //撤单数量 成交数量
    public BigDecimal count;

    public BigDecimal price;
}
