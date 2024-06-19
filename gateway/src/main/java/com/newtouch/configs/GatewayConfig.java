package com.newtouch.configs;

import com.newtouch.handler.impl.ConnectHandler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GatewayConfig {
    @Value("${counter.gateway-port}")
    private Integer gatewayPort;

    private final Vertx vertx = Vertx.vertx();

    @PostConstruct
    private void startup() {
        // 1.启动TCP服务监听 （来自柜台机的请求）
        initReceive();
    }

    // 初始化接收端 （来自柜台）
    private void initReceive() {
        // 1.创建tcp服务端（柜台机 会来連接）
        NetServer netServer = vertx.createNetServer();

        // 2.设置連接处理器
        NetServer server = netServer.connectHandler(new ConnectHandler());

        // 3.监听端口 端口是否连接成功handler
        server.listen(gatewayPort, res -> {
            if (res.succeeded()) {
                log.info("網關啟動成功, 端口 : {}", gatewayPort);
            } else {
                log.error("網關啟動失敗");
            }
        });
    }
}
