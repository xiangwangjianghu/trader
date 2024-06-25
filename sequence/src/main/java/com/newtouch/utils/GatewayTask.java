package com.newtouch.utils;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.sofa.jraft.util.Bits;
import com.alipay.sofa.jraft.util.BytesUtil;
import com.newtouch.configs.SequenceConfig;
import com.newtouch.dto.order.OrderPackDTO;
import com.newtouch.dto.order.OrderRequest;
import com.newtouch.service.GatewayService;
import com.newtouch.transport.codec.IBodyCodec;
import com.newtouch.transport.codec.impl.BodyCodec;
import io.vertx.core.buffer.Buffer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.TimerTask;

@Slf4j
@RequiredArgsConstructor
public class GatewayTask extends TimerTask {
    @NonNull
    private SequenceConfig sequenceConfig;

    private final IBodyCodec bodyCodec = new BodyCodec();

    private static final byte[] PACKET_NO_KEY = BytesUtil.writeUtf8("seq_pqcket_no");


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
        try {
            // 1.操作KV Store
            long packetNo = getPacketNoFromStore();
            byte[] serialize = bodyCodec.serialize(new OrderPackDTO(packetNo, orderRequestList));

            insertToKvStore(packetNo, serialize);
            updatePacketNoInStore(packetNo + 1);

            // TODO
            // 2.发送到撮合核心
            sequenceConfig.getDatagramSocket().send(
                    Buffer.buffer(serialize),
                    sequenceConfig.getMulticastPort(),
                    sequenceConfig.getMulticastIp(),
                    null
            );

        } catch (Exception e) {
            log.error("編碼訂單請求包出錯", e);
        }
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

    // 保存PacketNo
    private void insertToKvStore(long packetNo, byte[] serialize) {
        byte[] key = new byte[8];
        Bits.putLong(key, 0, packetNo);

        sequenceConfig.getSequenceNode().getRheaKVStore().put(key, serialize);
    }

    // 获取PacketNo
    private long getPacketNoFromStore() {
        byte[] bPacketNo = sequenceConfig.getSequenceNode().getRheaKVStore().bGet(PACKET_NO_KEY);

        return ArrayUtils.isNotEmpty(bPacketNo) ? Bits.getLong(bPacketNo, 0) : 0L;
    }

    // 更新PacketNo
    private void updatePacketNoInStore(long packetNo) {
        final byte[] bytes = new byte[8];
        Bits.putLong(bytes, 0, packetNo);

        sequenceConfig.getSequenceNode().getRheaKVStore().put(PACKET_NO_KEY, bytes);
    }

}
