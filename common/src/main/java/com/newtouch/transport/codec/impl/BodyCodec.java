package com.newtouch.transport.codec.impl;

import com.alipay.remoting.serialization.SerializerManager;
import com.newtouch.transport.codec.IBodyCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * alipay Hessian2序列化
 */
@Slf4j
@Component
public class BodyCodec implements IBodyCodec {
    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        return SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(bytes, clazz.getName());
    }
}
