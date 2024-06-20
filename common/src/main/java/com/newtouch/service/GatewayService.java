package com.newtouch.service;

import com.newtouch.dto.OrderRequest;

import java.util.List;

public interface GatewayService {

    List<OrderRequest> fetchOrderData();
}
