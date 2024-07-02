package com.newtouch.consumer;

import com.newtouch.utils.TCPUtil;
import io.vertx.core.Vertx;
import io.vertx.ext.bridge.BridgeEventType;
import io.vertx.ext.bridge.BridgeOptions;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebSocketConsumer {
    //五档行情
    public static final String L1_MARKET_DATA_PREFIX = "l1-market-data";

    //成交
    public final static String TRADE_NOTIFY_ADDR_PREFIX = "tradechange-";

    //委托
    public final static String ORDER_NOTIFY_ADDR_PREFIX = "orderchange-";

    @Autowired
    private TCPUtil tcpUtil;

    private final Vertx vertx = tcpUtil.vertx;

    @Value("${counter.pub-ip}")
    private short pubIp;

    @Value("${counter.pub-port}")
    private short pubPort;

    @PostConstruct
    private void init() {
        BridgeOptions options = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions().setAddress(L1_MARKET_DATA_PREFIX))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex(ORDER_NOTIFY_ADDR_PREFIX + "[0-9]+"))
                .addOutboundPermitted(new PermittedOptions().setAddressRegex(TRADE_NOTIFY_ADDR_PREFIX + "[0-9]+"));

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
        sockJSHandler.bridge((SockJSBridgeOptions) options, event -> {
            if (event.type() == BridgeEventType.SOCKET_CREATED) {
                log.info("客戶端 : {} 連接", event.socket().remoteAddress());
            } else if (event.type() == BridgeEventType.SOCKET_CREATED) {
                log.info("客戶端 : {} 關閉", event.socket().remoteAddress());
            }

            event.complete(true);
        });

        Router router = Router.router(vertx);
        router.route("/eventbus/*").handler(sockJSHandler);

        vertx.createHttpServer().requestHandler(router).listen(pubPort);
    }
}
