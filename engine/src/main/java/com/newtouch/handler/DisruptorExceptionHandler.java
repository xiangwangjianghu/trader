package com.newtouch.handler;

import com.lmax.disruptor.ExceptionHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

// 前置风控处理器
@Slf4j
@Data
@Component
public class DisruptorExceptionHandler<F> implements ExceptionHandler<F> {

    private String name = "main";
    private BiConsumer<Throwable, Long> onException = (t, l) -> {
        log.error("出現異常: {} , 序列號: {}", t, l);
    };

    @Override
    public void handleEventException(Throwable t, long l, F f) {
        if (log.isDebugEnabled()) {
            log.debug("Disruptor '{}', 異常: {}, 序列號: {} ", name, t, l);
        }

        //回调报错 （构造入参传入）
        onException.accept(t, l);
    }

    @Override
    public void handleOnStartException(Throwable t) {
        if (log.isDebugEnabled()) {
            log.debug("啟動異常: {}", name, t);
        }
    }

    @Override
    public void handleOnShutdownException(Throwable t) {
        if (log.isDebugEnabled()) {
            log.debug("關閉異常: {}", name, t);
        }
    }
}
