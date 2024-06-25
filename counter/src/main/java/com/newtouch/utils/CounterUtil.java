package com.newtouch.utils;

import com.newtouch.transport.checksum.ICheckSum;
import com.newtouch.transport.codec.IBodyCodec;
import com.newtouch.transport.codec.IMsgCodec;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CounterUtil {

    @Autowired
    private ICheckSum checkSum;

    @Autowired
    private IBodyCodec bodyCodec;

    @Autowired
    private IMsgCodec msgCodec;

    @Autowired
    private TCPUtil tcpUtil;

    @Value("${counter.id}")
    private Short counterId;

    @Value("${counter.gateway-id}")
    private Short gatewayId;

    @PostConstruct
    private void init() {

    }
}
