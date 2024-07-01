package com.newtouch.orderbook.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.newtouch.dto.MatchEvent;
import com.newtouch.dto.RingBufferRequest;
import com.newtouch.dto.market.L1MarketData;
import com.newtouch.enums.OrderResponseEnum;
import com.newtouch.enums.OrderStatusEnum;
import com.newtouch.orderbook.IOrderBook;
import com.newtouch.orderbook.IOrderBucket;
import com.newtouch.orderbook.MatchOrder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class OrderBookImpl implements IOrderBook {

    /**
     * 每个股票都有自己的订单簿 这里定义股票代码
     */
    @NonNull
    private Integer code;

    // 買賣行情: <價格,IOrderBucket>
    private final NavigableMap<BigDecimal, IOrderBucket> sellBuckets = new TreeMap<>();
    private final NavigableMap<BigDecimal, IOrderBucket> buyBuckets = new TreeMap<>(Comparator.reverseOrder());
    // 委託列表 <订单号,订单>
    private final LongObjectHashMap<MatchOrder> oidOrderMap = new LongObjectHashMap<>();

    @Override
    public OrderResponseEnum newOrder(RingBufferRequest ringBufferRequest) {
        if (oidOrderMap.contains(ringBufferRequest.getOid())) {
            return OrderResponseEnum.DUPLICATE_ORDER_ID;
        }


        // S 50 100  买单Buckets >=50 所有OrderBucket
        // B 40 200  卖单Buckets <=40 符合条件
        // 1.根据订单方向获取对手单数组
        NavigableMap<BigDecimal, IOrderBucket> contraBuckets = (ringBufferRequest.getDirection() == 0 ? sellBuckets : buyBuckets)
                .headMap(ringBufferRequest.getPrice(), true);
        // 2.预撮合
        BigDecimal deal = preMatch(ringBufferRequest, contraBuckets);
        if (deal.compareTo(ringBufferRequest.getCount()) == 0) {
            return OrderResponseEnum.SUCCESS;
        }

        if (deal.compareTo(new BigDecimal(0L)) == 0) {
            createMatchEvent(ringBufferRequest, OrderStatusEnum.ORDER_ED);
        } else {
            createMatchEvent(ringBufferRequest, OrderStatusEnum.PART_TRADE);
        }

        // 3.加入IOrderBucket
        MatchOrder matchOrder = MatchOrder.builder()
                .uid(ringBufferRequest.getUid())
                .code(ringBufferRequest.getCode())
                .direction(ringBufferRequest.getDirection())
                .price(ringBufferRequest.getPrice())
                .count(ringBufferRequest.getCount())    // 下单量
                .deal(deal)
                .oid(ringBufferRequest.getOid())
                .timestamp(ringBufferRequest.getTimestamp())
                .build();
        IOrderBucket bucket = (ringBufferRequest.getDirection() == 1 ? sellBuckets : buyBuckets)
                .computeIfAbsent(ringBufferRequest.getPrice(), price -> {
                    final IOrderBucket b = new OrderBucketImpl();
                    b.setPrice(price);
                    return b;
                });

        bucket.add(matchOrder);

        // 4.加入订单集合
        oidOrderMap.put(ringBufferRequest.getOid(), matchOrder);

        return OrderResponseEnum.SUCCESS;
    }

    private BigDecimal preMatch(RingBufferRequest ringBufferRequest, NavigableMap<BigDecimal, IOrderBucket> contraBuckets) {
        // 已經撮合量
        BigDecimal deal = new BigDecimal(0L);
        if (contraBuckets.isEmpty()) {
            return deal;
        }

        List<BigDecimal> emptyBuckets = Lists.newArrayList();
        for (IOrderBucket bucket : contraBuckets.values()) {
            deal = deal.add(bucket.match(ringBufferRequest.getCount().subtract(deal), ringBufferRequest, order -> oidOrderMap.remove(order.getOid())));

            if (bucket.getTotalCount().compareTo(new BigDecimal(0L)) == 0) {
                emptyBuckets.add(bucket.getPrice());
            }

            if (deal.compareTo(ringBufferRequest.getCount()) == 0) {
                break;
            }
        }

        emptyBuckets.forEach(contraBuckets::remove);
        return deal;
    }

    private void createMatchEvent(RingBufferRequest ringBufferRequest, OrderStatusEnum orderStatusEnum) {
        long tid = IdWorker.getId();

        MatchEvent matchEvent = MatchEvent.builder()
                .oid(ringBufferRequest.getOid())
                .count(new BigDecimal(0L))
                .status(orderStatusEnum)
                .tid(tid)
                .timestamp(System.currentTimeMillis())
                .build();

        ringBufferRequest.getMatchEventList().add(matchEvent);
    }

    @Override
    public OrderResponseEnum cancelOrder(RingBufferRequest ringBufferRequest) {
        // 1.从缓存中移除委托
        MatchOrder matchOrder = oidOrderMap.get(ringBufferRequest.getOid());
        if (matchOrder == null) {
            return OrderResponseEnum.INVALID_ORDER_ID;
        }
        oidOrderMap.remove(matchOrder.getOid());

        // 2.从对应orderbucket中移除委托
        NavigableMap<BigDecimal, IOrderBucket> buckets = matchOrder.getDirection() == 1 ? sellBuckets : buyBuckets;
        IOrderBucket orderBucket = buckets.get(matchOrder.getPrice());

        orderBucket.delete(matchOrder.getOid());
        if (orderBucket.getTotalCount().compareTo(new BigDecimal(0L)) == 0) {
            buckets.remove(matchOrder.getPrice());
        }

        // 3.发送撤单MatchEvent
        MatchEvent cancelEvent = MatchEvent.builder()
                .oid(matchOrder.getOid())
                .status(matchOrder.getDeal().compareTo(new BigDecimal(0L)) == 0 ? OrderStatusEnum.CANCEL_ED : OrderStatusEnum.PART_CANCEL)
                .count(matchOrder.getCount().subtract(matchOrder.getDeal()))
                .timestamp(System.currentTimeMillis())
                .build();
        ringBufferRequest.getMatchEventList().add(cancelEvent);

        return OrderResponseEnum.SUCCESS;
    }

    @Override
    public void fillBuys(int size, L1MarketData data) {
        if (size == 0) {
            data.setBuySize(0);
            return;
        }

        int i = 0;
        for (IOrderBucket bucket : buyBuckets.values()) {
            data.buyPrices[i] = bucket.getPrice();
            data.buyCounts[i] = bucket.getTotalCount();

            if (++i == size) {
                break;
            }
        }

        // 已經填充行情檔位
        data.setBuySize(i);
    }

    @Override
    public void fillSells(int size, L1MarketData data) {
        if (size == 0) {
            data.setSellSize(0);
            return;
        }

        int i = 0;
        for (IOrderBucket bucket : sellBuckets.values()) {
            data.sellPrices[i] = bucket.getPrice();
            data.sellCounts[i] = bucket.getTotalCount();

            if (++i == size) {
                break;
            }
        }

        // 已經填充行情檔位
        data.setSellSize(i);
    }

    @Override
    public void fillCode(L1MarketData data) {
        data.setCode(code);
    }

    @Override
    public int limitBuyBucketSize(int maxSize) {
        return Math.min(maxSize, buyBuckets.size());
    }

    @Override
    public int limitSellBucketSize(int maxSize) {
        return Math.min(maxSize, sellBuckets.size());
    }
}
