package com.newtouch.utils;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.alipay.sofa.jraft.util.Bits;
import com.google.common.collect.Lists;
import com.newtouch.dto.order.OrderPackDTO;
import com.newtouch.dto.order.OrderRequest;
import com.newtouch.transport.codec.IBodyCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OrderPacketQueue {

    @Autowired
    @Qualifier(value = "asyncGatewayExecutor")
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private DisruptorUtil disruptorUtil;

    @Autowired
    private IBodyCodec bodyCodec;

    private final BlockingQueue<OrderPackDTO> queue = new LinkedBlockingQueue<>();

    public int size() {
        return queue.size();
    }

    // 接收订单
    public boolean offer(OrderPackDTO orderPackDTO) {
        return queue.offer(orderPackDTO);
    }

    // 抓取數據
    public void handleOrderPack(RheaKVStore kvStore) {

        ExecutorCompletionService<Object> completionService = new ExecutorCompletionService<>(executor);
        completionService.submit(() -> {
            while (true) {
                try {
                    OrderPackDTO orderPackDTO = queue.poll(10, TimeUnit.SECONDS);

                    if (ObjectUtils.isNotEmpty(orderPackDTO)) {
                        handle(kvStore, orderPackDTO);
                    }
                } catch (Exception e) {
                    log.error("接收訂單請求包失敗,繼續");
//                    break;
                }
            }
        });
    }

    private long lastPackNo = -1;

    // 处理下单数据
    private void handle(RheaKVStore kvStore, OrderPackDTO orderPackDTO) {
        log.info("接收訂單請求包 : {}", JSONObject.toJSONString(orderPackDTO));

        long packNo = orderPackDTO.getPackNo();

        if (packNo == lastPackNo + 1) {     // 有序收包
            for (OrderRequest orderRequest : orderPackDTO.getOrderRequestList()) {
                disruptorUtil.submit(orderRequest);
            }
        } else if (packNo <= lastPackNo) {  // 丟棄来自歷史的重複的包
            log.info("收到重複請求包 : {}", packNo);
        } else {                            // 跳号
            log.info("packNo上次從 {} 到 {},開始從排隊機重新查詢", lastPackNo + 1, packNo);

            byte[] startKey = new byte[8];
            byte[] endKey = new byte[8];
            Bits.putLong(startKey, 0, lastPackNo + 1);
            Bits.putLong(endKey, 0, packNo + 1);

            final List<KVEntry> lostSection = kvStore.bScan(startKey, endKey);
            List<OrderPackDTO> oderPackDTOList = Lists.newArrayList();

            for (KVEntry kvEntry : lostSection) {
                byte[] value = kvEntry.getValue();

                if (ArrayUtils.isNotEmpty(value)) {
                    try {
                        oderPackDTOList.add(bodyCodec.deserialize(value, OrderPackDTO.class));
                    } catch (Exception e) {
                        log.error("解碼訂單請求包出錯", e);
                    }
                }

                List<OrderPackDTO> sortedPackList = oderPackDTOList.stream().filter(pack -> CollectionUtils.isNotEmpty(pack.getOrderRequestList()))
                        .sorted((o1, o2) -> (int) (o1.getPackNo() - o2.getPackNo())).toList();

                for (OrderPackDTO pack : sortedPackList) {
                    for (OrderRequest orderRequest : pack.getOrderRequestList()) {
                        disruptorUtil.submit(orderRequest);
                    }
                }
            }

            // 重置包號
            lastPackNo = packNo;
        }
    }
}
