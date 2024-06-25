package com.newtouch.utils;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.newtouch.dto.order.OrderRequest;
import com.newtouch.dto.RingBufferRequest;
import com.newtouch.enums.OrderResponseEnum;
import com.newtouch.handler.DisruptorExceptionHandler;
import com.newtouch.handler.L1PubHandler;
import com.newtouch.handler.MatchHandler;
import com.newtouch.handler.RiskHandler;
import lombok.extern.slf4j.Slf4j;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Timer;

import static com.newtouch.enums.OrderRequestTypeConstants.*;
import static com.newtouch.handler.L1PubHandler.HQ_PUB_RATE;

@Component
@Slf4j
public class DisruptorUtil {

    @Autowired
    private DisruptorExceptionHandler<RingBufferRequest> exceptionHandler;

    @Autowired
    private PubTask pubTask;

    private static final int RING_BUFFER_SIZE = 1024;

    private final Disruptor<RingBufferRequest> disruptor = new Disruptor<>(
            new RingBufferFactory(),       // 事件工厂类
            RING_BUFFER_SIZE,             // 队列大小 2n次幂 会进行位运算
            new AffinityThreadFactory("親和線程", AffinityStrategies.SAME_CORE),    // 亲和线程池 线程亲和性能够强制使你的应用线程运行在特定的一个或多个cpu上
            ProducerType.SINGLE,         // 一个生产者
            new BlockingWaitStrategy()   // 等待策略 阻塞队列
    );

    private final RingBuffer<RingBufferRequest> ringBuffer = disruptor.getRingBuffer();

    public void startDisruptor(RiskHandler riskHandler, MatchHandler matchHandler, L1PubHandler l1PubHandler) {
        disruptor.setDefaultExceptionHandler(exceptionHandler);

        // 前置风控 --> 撮合 --> 发布数据
        disruptor.handleEventsWith(riskHandler)
                .then(matchHandler)
                .then(l1PubHandler);

        disruptor.start();

        // 定时发布行情
        new Timer().schedule(pubTask, 5000, HQ_PUB_RATE);
    }

    public void submit(OrderRequest orderRequest) {
        switch (orderRequest.getOrderRequestType()) {
            case HQ_PUB:        // 行情
                ringBuffer.publishEvent(HQ_PUB_TRANSLATOR, orderRequest);
                break;
            case NEW_ORDER:     // 下单
                ringBuffer.publishEvent(NEW_ORDER_TRANSLATOR, orderRequest);
                break;
            case CANCEL_ORDER:  // 撤单
                ringBuffer.publishEvent(CANCEL_ORDER_TRANSLATOR, orderRequest);
                break;
            default:
                throw new IllegalStateException("不支持的請求類型: " + orderRequest.getOrderType());
        }
    }

    private static final EventTranslatorOneArg<RingBufferRequest, OrderRequest> HQ_PUB_TRANSLATOR = (r, l, o) -> {
        r.setOrderRequestType(HQ_PUB);
        r.setOrderResponseEnum(OrderResponseEnum.SUCCESS);
    };

    private static final EventTranslatorOneArg<RingBufferRequest, OrderRequest> NEW_ORDER_TRANSLATOR = (r, l, o) -> {
        r.setOrderRequestType(NEW_ORDER);
        r.setOrderResponseEnum(OrderResponseEnum.SUCCESS);

        r.setTimestamp(o.getTimestamp());
        r.setUid(o.getUid());
        r.setCode(o.getCode());
        r.setDirection(o.getDirection());
        r.setPrice(o.getPrice());
        r.setCount(o.getCount());
        r.setType(o.getOrderType());
        r.setOid(o.getCounterOid());
    };

    private static final EventTranslatorOneArg<RingBufferRequest, OrderRequest> CANCEL_ORDER_TRANSLATOR = (r, l, o) -> {
        r.setOrderRequestType(CANCEL_ORDER);
        r.setOrderResponseEnum(OrderResponseEnum.SUCCESS);

        r.setTimestamp(o.getTimestamp());
        r.setUid(o.getUid());
        r.setCode(o.getCode());
        r.setOid(o.getCounterOid());
    };
}
