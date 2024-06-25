package com.newtouch.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.newtouch.dto.market.L1MarketData;
import com.newtouch.enums.OrderResponseEnum;
import io.netty.util.collection.IntObjectHashMap;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Slf4j
public class RingBufferRequest {

    Long uid;

    Long oid;   // 訂單編號

    Integer code;

    Integer direction; // 0: 買入 1: 賣出

    Integer type;      // 0: 市價單  1: 限價單

    Integer orderRequestType;      // 訂單請求類型

    BigDecimal price;

    BigDecimal count;

    Long timestamp;

    List<MatchEvent> matchEventList;    // 撮合數據結構

    OrderResponseEnum orderResponseEnum;    // 前置风控 --> 撮合 --> 发布

    IntObjectHashMap<L1MarketData> marketDataMap; // 行情
}
