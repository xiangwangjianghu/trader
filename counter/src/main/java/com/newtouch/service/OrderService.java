package com.newtouch.service;

import com.newtouch.dto.OrderRequest;
import com.newtouch.entity.Order;
import com.newtouch.enums.ResponseEnum;

import java.util.List;

public interface OrderService {

    List<Order> getOrderList(long uid);

    ResponseEnum sendOrder(OrderRequest orderRequest);

    ResponseEnum cancelOrder(OrderRequest orderRequest);
}
