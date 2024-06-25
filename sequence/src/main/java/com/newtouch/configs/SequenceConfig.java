package com.newtouch.configs;

import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MemoryDBOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.StoreEngineOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.util.Endpoint;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.google.common.collect.Maps;
import com.newtouch.dto.SequenceNode;
import com.newtouch.listener.GatewayChannelListener;
import com.newtouch.service.GatewayService;
import com.newtouch.utils.GatewayTask;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Timer;

@Slf4j
@Component
public class SequenceConfig {
    @Value("#{'${sequence.data-paths:}'.empty ? null : '${sequence.data-paths:}'.split(',')}")
    private List<String> dataPathList;

    @Value("#{'${sequence.serve-urls:}'.empty ? null : '${sequence.serve-urls:}'.split(',')}")
    private List<String> serveUrlList;

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Value("${engine.multicast-ip}")
    @Getter
    private String multicastIp;

    @Value("${engine.multicast-port}")
    @Getter
    private Integer multicastPort;

    @Getter
    private SequenceNode sequenceNode;

    @Getter
    private DatagramSocket datagramSocket;

    public static Map<String, GatewayService> gatewayServiceMap = Maps.newConcurrentMap();

    public void startup(int sequence) {
        // 啟動kv store集群
        startSequenceNode(sequence);

        // 啟動下游广播
        startMultiCast();

        // 連接網關
        startGateway();
    }


    // 建立到网关的连接, 併抓取訂單數據
    private void startGateway() {
        ConsumerConfig<GatewayService> consumerConfig = new ConsumerConfig<GatewayService>()
                .setInterfaceId(GatewayService.class.getName())
                .setProtocol("bolt")
                .setTimeout(5000)
                .setDirectUrl(gatewayUrl);

        // 消费者链接处理器
        GatewayChannelListener channelListener = new GatewayChannelListener(consumerConfig);
        consumerConfig.setOnConnect(List.of(channelListener));

        // 获取引用代理类
        GatewayService gatewayService = consumerConfig.refer();
        gatewayServiceMap.put(gatewayUrl, gatewayService);

        // 定时抓取数据
        new Timer().schedule(new GatewayTask(this), 5000, 1000);

    }

    // 启动KV Store
    private void startSequenceNode(int sequence) {
        final PlacementDriverOptions pdOpts = PlacementDriverOptionsConfigured.newConfigured()
                .withFake(true)
                .config();

        // 127.0.0.1:8891,127.0.0.1:8892,127.0.0.1:8893
        String[] split = serveUrlList.get(sequence).split(":");
        final StoreEngineOptions storeOpts = StoreEngineOptionsConfigured.newConfigured()
                .withStorageType(StorageType.Memory)    // 存储形式 内存
                .withMemoryDBOptions(MemoryDBOptionsConfigured.newConfigured().config()) // 内存DB 默认设置
                .withRaftDataPath(dataPathList.get(sequence)) // 存储位置
                .withServerAddress(new Endpoint(split[0], Integer.parseInt(split[1])))   // 服务地址
                .config();

        // kv 数据库配置 配置前面的配置
        final RheaKVStoreOptions kvOpts = RheaKVStoreOptionsConfigured.newConfigured()
                .withInitialServerList(StringUtils.join(serveUrlList, ","))
                .withStoreEngineOptions(storeOpts)
                .withPlacementDriverOptions(pdOpts)
                .config();

        // 开启节点
        sequenceNode = new SequenceNode(kvOpts);
        sequenceNode.start();

        // 监听停止 执行节点关闭
        Runtime.getRuntime().addShutdownHook(new Thread(sequenceNode::stop));
        log.info("成功啟動排隊機節點 : {}, 地址 : {}", sequence, serveUrlList.get(sequence));
    }

    // 下游广播
    private void startMultiCast() {
        datagramSocket = Vertx.vertx().createDatagramSocket(new DatagramSocketOptions());
    }
}
