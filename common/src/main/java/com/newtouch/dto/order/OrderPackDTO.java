package com.newtouch.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderPackDTO {

    /**
     * 发送包序号
     */

    private long packNo;

    /**
     * 下单命令
     */
    private List<OrderRequest> orderRequestList;
}
