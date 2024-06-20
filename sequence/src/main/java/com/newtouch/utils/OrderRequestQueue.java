package com.newtouch.utils;

import com.newtouch.dto.OrderRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class OrderRequestQueue {

    private final BlockingQueue<OrderRequest> queue = new LinkedBlockingQueue<>();

    public int size() {
        return queue.size();
    }

    // 提交订单
    public boolean offer(OrderRequest orderRequest) {
        return queue.offer(orderRequest);
    }

    // 去除全部数据 清空缓存集合
    public List<OrderRequest> removeAndGetAll() {
        List<OrderRequest> orderRequestList = new ArrayList<>();
        queue.drainTo(orderRequestList);

        return orderRequestList;
    }
}
