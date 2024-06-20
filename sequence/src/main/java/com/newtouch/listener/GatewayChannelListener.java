package com.newtouch.listener;

import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.listener.ChannelListener;
import com.alipay.sofa.rpc.transport.AbstractChannel;
import com.google.common.collect.Maps;
import com.newtouch.configs.SequenceConfig;
import com.newtouch.service.GatewayService;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class GatewayChannelListener implements ChannelListener {

    @NonNull
    private ConsumerConfig<GatewayService> consumerConfig;

    /**
     * 第一次创建链接不会执行 onConnected
     * 断开重连才会进入
     *
     * @param channel
     */
    @Override
    public void onConnected(AbstractChannel channel) {
        String remoteAddr = channel.remoteAddress().toString();
        log.info("連接到網關 : {}", remoteAddr);

        // 保存当前链接
        SequenceConfig.gatewayServiceMap.put(remoteAddr, consumerConfig.refer());
    }

    @Override
    public void onDisconnected(AbstractChannel channel) {
        String remoteAddr = channel.remoteAddress().toString();
        log.info("斷開網關連接 : {}", remoteAddr);

        // 斷開当前链接
        SequenceConfig.gatewayServiceMap.remove(remoteAddr);
    }
}
