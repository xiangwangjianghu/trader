package com.newtouch.dto.market;

import com.newtouch.enums.OrderStatusEnum;
import lombok.Builder;
import lombok.Data;

// 發佈到總線櫃檯
@Builder
@Data
public class MatchData {
    public long timestamp;

    public long oid;

    public Integer status;

    public long tid;

    //撤单数量 成交数量
    public long count;

    public long price;
}
