package com.newtouch.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import lombok.Data;

@Data
public class StockRequest {

    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Integer code;

    String name;

    String abbrName;

    short status;
}
