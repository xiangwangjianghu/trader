package com.newtouch.configs;

import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.google.common.collect.Maps;
import com.newtouch.bus.impl.MqttBusSender;
import com.newtouch.dto.RingBufferRequest;
import com.newtouch.dto.order.OrderPackDTO;
import com.newtouch.handler.L1PubHandler;
import com.newtouch.handler.MatchHandler;
import com.newtouch.handler.RiskHandler;
import com.newtouch.orderbook.IOrderBook;
import com.newtouch.orderbook.impl.OrderBookImpl;
import com.newtouch.service.GatewayService;
import com.newtouch.service.StockService;
import com.newtouch.service.UserService;
import com.newtouch.transport.codec.IBodyCodec;
import com.newtouch.utils.DisruptorUtil;
import com.newtouch.utils.OrderPacketQueue;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

@Slf4j
@Component
public class EngineConfig {
    @Value("${sequence.serve-urls}")
    private String serveUrlList;

    @Value("${engine.multicast-ip}")
    private String multicastIp;

    @Value("${engine.multicast-port}")
    private Integer multicastPort;

    @Autowired
    private OrderPacketQueue orderPacketQueue;

    @Autowired
    private IBodyCodec bodyCodec;

    @Autowired
    private StockService stockService;

    @Autowired
    private UserService userService;

    @Autowired
    private DisruptorUtil disruptorUtil;

    @Autowired
    public MqttBusSender mqttBusSender;

    private final RheaKVStore orderKVStore = new DefaultRheaKVStore();
    private final Vertx vertx = Vertx.vertx();

    public void startup(int sequence) {
        // 啟動撮合核心
        startEngine();

        // 連接總線
        startBus();

        // 連接排隊機
        startSequence();
    }

    // 啟動撮合核心
    private void startEngine() {
        // 1.前置風控
        MutableLongSet uidSet = userService.getBalanceList().keySet();
        IntHashSet codeSet = stockService.getStockList();
        RiskHandler riskHandler = new RiskHandler(uidSet, codeSet);

        // 2.撮合
        IntObjectHashMap<IOrderBook> orderBookMap = new IntObjectHashMap<>();
        // 每个股票代码都定义订单簿
        codeSet.forEach(code -> orderBookMap.put(code, new OrderBookImpl(code)));
        MatchHandler matchHandler = new MatchHandler(orderBookMap);

        // 3.發佈行情
        L1PubHandler l1PubHandler = new L1PubHandler();

        // 4.啟動Disruptor
        disruptorUtil.startDisruptor(riskHandler, matchHandler, l1PubHandler);
    }

    // 連接總線
    private void startBus() {
        mqttBusSender.startup(vertx);
    }

    // 連接排隊機
    private void startSequence() {
        final List<RegionRouteTableOptions> regionRouteTableOptions = MultiRegionRouteTableOptionsConfigured.newConfigured().withInitialServerList(-1L, serveUrlList).config();
        final PlacementDriverOptions pdOpts = PlacementDriverOptionsConfigured.newConfigured().withFake(true).withRegionRouteTableOptionsList(regionRouteTableOptions).config();
        final RheaKVStoreOptions opts = RheaKVStoreOptionsConfigured.newConfigured().withPlacementDriverOptions(pdOpts).config();

        // 初始化一致性数据缓存,抓取數據
        orderKVStore.init(opts);
        orderPacketQueue.handleOrderPack(orderKVStore);

        // 组播接受 允许多个Socket接收同一份数据
        DatagramSocket socket = vertx.createDatagramSocket(new DatagramSocketOptions());
        socket.listen(multicastPort, "0.0.0.0", asyncRes1 -> {
            if (asyncRes1.succeeded()) {
                socket.handler(packet -> {
                    Buffer udpData = packet.data();

                    if (ObjectUtils.isNotEmpty(udpData)) {
                        try {
                            orderPacketQueue.offer(bodyCodec.deserialize(udpData.getBytes(), OrderPackDTO.class));
                        } catch (Exception e) {
                            log.error("解碼訂單請求包出錯: ", e);
                        }
                    } else {
                        log.error("從客戶端接收到空包 : {}", packet.sender().toString());
                    }
                });

                try {
                    socket.listenMulticastGroup(multicastIp, mainInterface().getName(), null, asyncRes2 -> {
                        log.info("監聽結果: {}", asyncRes2.succeeded());
                    });
                } catch (Exception e) {
                    log.error("監聽組播失敗, 原因: ", e);
                }
            } else {
                log.error("撮合引擎監聽排隊機失敗, 原因: ", asyncRes1.cause());
            }
        });
    }

    // 獲取網卡
    private NetworkInterface mainInterface() throws SocketException {
        final ArrayList<NetworkInterface> interfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());

        // 1. !loopback
        // 2. 非虚拟机网卡
        // 3. 支持multicast
        // 4. 有IPV4

        return interfaceList.stream().filter(i -> {
            try {
                return !i.isLoopback() &&
                        !(i.getDisplayName().contains("VirtualBox") || i.getDisplayName().contains("Host-only")) &&
                        i.supportsMulticast() &&
                        i.getInterfaceAddresses().stream().anyMatch(a -> a.getAddress() instanceof Inet4Address);
            } catch (Exception e) {
                log.error("尋找網卡出錯: ", e);
            }

            return false;
        }).min(Comparator.comparing(NetworkInterface::getName)).orElse(null);
    }
}
