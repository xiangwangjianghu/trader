package com.newtouch.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {

    // insert/update自動返回主鍵
    Long id;

    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long uid;

    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    Integer code;

    Integer direction; // 0: 買入 1: 賣出

    Integer type;      // 0: 市價單  1: 限價單

    @JsonDeserialize(using = NumberDeserializers.BigDecimalDeserializer.class)
    BigDecimal price;

    @JsonDeserialize(using = NumberDeserializers.BigDecimalDeserializer.class)
    BigDecimal count;

    Integer status;

    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
    Long counterOid;    // 组装ID [  柜台ID,  委托ID ]
}
