package com.newtouch.handler;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.lmax.disruptor.EventHandler;
import com.newtouch.configs.EngineConfig;
import com.newtouch.dto.MatchEvent;
import com.newtouch.dto.RingBufferRequest;
import com.newtouch.dto.market.L1MarketData;
import com.newtouch.dto.market.MatchData;
import com.newtouch.dto.order.CommonMsg;
import com.newtouch.enums.MsgConstants;
import com.newtouch.transport.checksum.ICheckSum;
import com.newtouch.transport.codec.IBodyCodec;
import io.netty.util.collection.IntObjectHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.collections.api.tuple.primitive.ShortObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.newtouch.enums.OrderRequestTypeConstants.*;

// 前置风控处理器
@Slf4j
@Component
public class L1PubHandler implements EventHandler<RingBufferRequest> {
    // 发布时间间隔
    public static final int HQ_PUB_RATE = 1000;
    // 发送数据 <counterId,List<MatchData>>
    private final ShortObjectHashMap<List<MatchData>> matchDataMap = new ShortObjectHashMap<>() {{
        put(counterId, Lists.newArrayList());
    }};
    // 櫃檯地址
    public static final short HQ_ADDRESS = -1;

    @Value("${engine.id}")
    private short engineId;

    @Value("${counter.id}")
    private short counterId;

    @Autowired
    private IBodyCodec bodyCodec;
    @Autowired
    private ICheckSum checkSum;
    @Autowired
    private EngineConfig engineConfig;

    @Override
    public void onEvent(RingBufferRequest ringBufferRequest, long sequence, boolean endOfBatch) throws Exception {
        Integer orderRequestType = ringBufferRequest.getOrderRequestType();
        if (orderRequestType == NEW_ORDER || orderRequestType == CANCEL_ORDER) {    // 下單撤單
            for (MatchEvent matchEvent : ringBufferRequest.getMatchEventList()) {
                matchDataMap.get(counterId).add(matchEvent.copyToMatchData());
            }
        } else if (orderRequestType == HQ_PUB) {    // 行情發佈
            // 1.發佈5檔行情
            pubMarketData(ringBufferRequest.getMarketDataMap());

            // 2.向櫃檯發送數據
            pubMatchData();
        }
    }

    private void pubMarketData(IntObjectHashMap<L1MarketData> marketDataMap) {
        byte[] serialize = null;
        try {
            serialize = bodyCodec.serialize(marketDataMap.values().toArray(new L1MarketData[0]));
        } catch (Exception e) {
            log.error("序列化行情失敗", e);
        }

        if (serialize != null) {
            CommonMsg msg = CommonMsg.builder()
                    .bodyLength(serialize.length)
                    .checksum(checkSum.getChecksum(serialize))
                    .msgSrc(engineId)
                    .msgDst(HQ_ADDRESS)
                    .msgType(MsgConstants.MATCH_HQ_DATA)
                    .status(MsgConstants.NORMAL)
                    .msgNo(IdWorker.getId())
                    .body(serialize)
                    .timestamp(System.currentTimeMillis())
                    .build();

            engineConfig.mqttBusSender.publish(msg);
        }
    }

    private void pubMatchData() {
        if (!matchDataMap.isEmpty()) {
            for (ShortObjectPair<List<MatchData>> pair : matchDataMap.keyValuesView()) {
                if (CollectionUtils.isNotEmpty(pair.getTwo())) {
                    byte[] serialize = null;
                    try {
                        serialize = bodyCodec.serialize(pair.getTwo().toArray(new MatchData[0]));
                    } catch (Exception e) {
                        log.error("序列化行情失敗", e);
                    }

                    if (serialize != null) {
                        CommonMsg msg = CommonMsg.builder()
                                .bodyLength(serialize.length)
                                .checksum(checkSum.getChecksum(serialize))
                                .msgSrc(engineId)
                                .msgDst(pair.getOne())
                                .msgType(MsgConstants.MATCH_HQ_DATA)
                                .status(MsgConstants.NORMAL)
                                .msgNo(IdWorker.getId())
                                .body(serialize)
                                .timestamp(System.currentTimeMillis())
                                .build();

                        engineConfig.mqttBusSender.publish(msg);
                    }
                }
            }
        }
    }
}
