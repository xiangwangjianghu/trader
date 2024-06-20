package com.newtouch.utils;

import com.alibaba.fastjson2.JSONObject;
import com.newtouch.configs.SequenceConfig;
import com.newtouch.dto.OrderRequest;
import com.newtouch.service.GatewayService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class GatewayTask extends TimerTask {
    @NonNull
    private SequenceConfig sequenceConfig;

    @Override
    public void run() {
        // 主节点才拉取数据
        if (!sequenceConfig.getSequenceNode().isLeader()) {
            return;
        }

        List<OrderRequest> orderRequestList = SequenceConfig.gatewayServiceMap.values().stream()
                .map(GatewayService::fetchOrderData).filter(CollectionUtils::isEmpty).flatMap(List::stream).toList();
        if (CollectionUtils.isEmpty(orderRequestList)) {
            return;
        }

        log.info("從網關抓取的訂單數據: {}", JSONObject.toJSONString(orderRequestList));

        // 排序 时间优先 价格优先 量优先
        orderRequestList.sort((o1, o2) -> {
            int res = compareTime(o1, o2);
            if (res != 0) {
                return res;
            }

            res = comparePrice(o1, o2);
            if (res != 0) {
                return res;
            }

            res = compareCount(o1, o2);
            return res;
        });

        // 存储到KV Store，发送到撮合核心
    }

    // 时间排序
    private int compareTime(OrderRequest o1, OrderRequest o2) {
        return o1.getTimestamp().compareTo(o2.getTimestamp());
    }

    // 价格排序
    private int comparePrice(OrderRequest o1, OrderRequest o2) {
        // 方向一致才比較
        if (!o1.getDirection().equals(o2.getDirection())) {
            return 0;
        }

        if (o1.getPrice().compareTo(o2.getPrice()) > 0) {
            return o1.getDirection().equals(0) ? -1 : 1;
        } else if (o1.getPrice().compareTo(o2.getPrice()) < 0) {
            return o1.getDirection().equals(1) ? -1 : 1;
        } else {
            return 0;
        }
    }

    // 委託量排序
    private int compareCount(OrderRequest o1, OrderRequest o2) {
        return o2.getCount().compareTo(o1.getCount());
    }
}
