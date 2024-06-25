package com.newtouch.service;

import com.newtouch.dto.order.OrderRequest;

import java.util.List;

public interface GatewayService {

    List<OrderRequest> fetchOrderData();
}
