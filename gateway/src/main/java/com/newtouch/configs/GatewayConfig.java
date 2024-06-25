package com.newtouch.configs;

import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.newtouch.handler.impl.ConnectHandler;
import com.newtouch.service.GatewayService;
import com.newtouch.utils.OrderRequestQueue;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GatewayConfig {

    @Autowired
    private ConnectHandler connectHandler;

    @Autowired
    private OrderRequestQueue orderRequestQueue;

    @Value("${counter.gateway-port}")
    private Integer gatewayPort;

    @Value("${counter.sequence-port}")
    private Integer sequencePort;

    private final Vertx vertx = Vertx.vertx();

    @PostConstruct
    private void startup() {
        // 1.启动TCP服务监听 （来自柜台机的请求）
        initReceive();

        // 2.排队机交互
        initSend();
    }

    // 初始化接收端 （来自柜台）
    private void initReceive() {
        // 1.创建tcp服务端（柜台机 会来連接）
        NetServer netServer = vertx.createNetServer();

        // 2.设置連接处理器
        NetServer server = netServer.connectHandler(connectHandler);

        // 3.监听端口 端口是否连接成功handler
        server.listen(gatewayPort, res -> {
            if (res.succeeded()) {
                log.info("監聽網關成功, 端口 : {}", gatewayPort);
            } else {
                log.error("網關啟動失敗");
            }
        });
    }

    // 初始化發送端 （送往排隊機）
    private void initSend() {
        ServerConfig config = new ServerConfig().setPort(sequencePort).setProtocol("bolt");
        ProviderConfig<GatewayService> providerConfig = new ProviderConfig<GatewayService>()
                .setInterfaceId(GatewayService.class.getName())
                .setRef(() -> orderRequestQueue.fetchOrderData())
                .setServer(config);

        providerConfig.export();
        log.info("監聽排隊機成功, 端口 : {}", sequencePort);
    }
}
