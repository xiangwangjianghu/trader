package com.newtouch.utils;

import com.alibaba.fastjson2.JSONObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

// 发送到网关
@Component
@Slf4j
public class TCPUtil {

    @Autowired
    @Qualifier(value = "asyncGatewayExecutor")
    private ThreadPoolTaskExecutor executor;

    public String senderIp;

    public int senderPort;

    public Vertx vertx = Vertx.vertx();

    /**
     * 取最新的（重连会重置）
     */
    private volatile NetSocket socket;


    /**
     * 队列作用 已解耦代码
     * socket自己去缓存中取数据进行发送
     * 客户请求写入队列
     * 单独线程去读队列 写到网关
     */
    private final BlockingQueue<Buffer> sendQueue = new LinkedBlockingDeque<>();

    public boolean send(Buffer buffer) {
        return sendQueue.offer(buffer);
    }

    // 处理链接类
    private class ClientConnHandler implements Handler<AsyncResult<NetSocket>> {
        @Override
        public void handle(AsyncResult<NetSocket> result) {
            // 链接创建成功 设置处理器
            if (result.succeeded()) {
                log.info("成功連接到 {}:{}", senderIp, senderPort);

                socket = result.result();
                // 链接关闭
                socket.closeHandler(close -> {
                    log.info("連接遠程 {} 關閉", socket.remoteAddress());

                    // 重连
                    reconnect();
                });

                // 链接保存
                socket.exceptionHandler(e -> log.error("連接出錯退出", e.getCause()));
            }
        }

        private void reconnect() {
            vertx.setTimer(5 * 1000, r -> {
                log.info("嘗試重新連接 {}:{} 失敗", senderIp, senderPort);

                vertx.createNetClient().connect(senderPort, senderIp, new ClientConnHandler());
            });
        }
    }

    // 啟動TCP連接
    public void startup() {
        // 开启与目标的链接 (创建一个TCP客户端 handle方法会得到 NetSocket)
        vertx.createNetClient().connect(senderPort, senderIp, new ClientConnHandler());

        // 单独启动一个线程取阻塞队列中的数据写入网关
        ExecutorCompletionService<Object> completionService = new ExecutorCompletionService<>(executor);
        completionService.submit(() -> {
            while (true) {
                try {
                    // 从BlockingQueue取出一个队首的对象，如果在指定时间内，
                    // 队列一旦有数据可取，则立即返回队列中的数据。否则知道时间,超时还没有数据可取，返回失败
                    Buffer buffer = sendQueue.poll(5, TimeUnit.SECONDS);

                    log.info("調用線程池");
                    if (ObjectUtils.allNotNull(buffer, socket)) {
                        // 远程链接写入
                        socket.write(buffer);
                        log.info("远程链接写入:{}", JSONObject.toJSONString(buffer));
                    }
                } catch (Exception e) {
                    log.error("消息發送失敗,繼續");
//                    break;
                }
            }
        });
    }
}
