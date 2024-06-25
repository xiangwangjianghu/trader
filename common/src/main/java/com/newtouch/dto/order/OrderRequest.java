package com.newtouch.dto.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderRequest {

    // insert/update自動返回主鍵
    Long id;

    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long uid;

    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    Integer code;

    Integer direction; // 0: 買入 1: 賣出

    int orderRequestType;      // 訂單請求類型

    Integer orderType;         // 訂單類型 0: 市價單  1: 限價單

    @JsonDeserialize(using = NumberDeserializers.BigDecimalDeserializer.class)
    BigDecimal price;

    @JsonDeserialize(using = NumberDeserializers.BigDecimalDeserializer.class)
    BigDecimal count;

    Integer status;

    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
    Long counterOid;    // 组装ID [  柜台ID,  委托ID ]

    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
    Long timestamp;
}
