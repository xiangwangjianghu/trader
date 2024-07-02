package com.newtouch.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.newtouch.dto.order.OrderRequest;
import com.newtouch.entity.Order;
import com.newtouch.enums.CacheEnum;
import com.newtouch.enums.OrderDirectionEnum;
import com.newtouch.enums.ResponseEnum;
import com.newtouch.mappers.OrderMapper;
import com.newtouch.mappers.UserMapper;
import com.newtouch.service.OrderService;
import com.newtouch.transport.codec.impl.BodyCodec;
import com.newtouch.utils.GatewayUtil;
import com.newtouch.utils.IdUtil;
import com.newtouch.utils.RedisUtil;
import com.newtouch.utils.TCPUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.newtouch.consumer.MatchDataConsumer.ORDER_DATA_CACHE_ADDR;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IdUtil idUtil;

    @Autowired
    private GatewayUtil gatewayUtil;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${counter.id}")
    private Long counterId;
    @Autowired
    private BodyCodec bodyCodec;

    @Autowired
    private TCPUtil tcpUtil;

    private final Vertx vertx = tcpUtil.vertx;

    @Override
    public List<Order> getOrderList(long uid) {
        List<Order> orderList = new ArrayList<>();

        String key = String.valueOf(uid);
        String orderCache = redisUtil.getCache(key, CacheEnum.ORDER);

        if (StringUtils.isEmpty(orderCache)) {
            orderList = orderMapper.selectListByUid(uid);
            redisUtil.addCache(key, JSON.toJSONString(orderList), CacheEnum.POSITION);
        } else {
            orderList = JSON.parseArray(orderCache, Order.class);
        }

        return orderList;
    }

    @Override
    public ResponseEnum sendOrder(OrderRequest orderRequest) {
        BigDecimal balance = userMapper.getBalance(orderRequest.getUid());
        BigDecimal amount = orderRequest.getCount().multiply(orderRequest.getPrice());
        amount = OrderDirectionEnum.BUY.getDirection().equals(orderRequest.getDirection()) ?
                amount.negate() : amount;

        if (balance.add(amount).compareTo(BigDecimal.ZERO) >= 0) {
            // 1.更新用戶餘額
            userMapper.updateBalance(orderRequest.getUid(), amount);
            // 2.插入訂單表
            orderMapper.sendOrder(orderRequest);

            // 3.生成全局ID  组装ID long [  柜台ID,  委托ID ]
            // 發送到總線
            long counterOid = idUtil.combineOid(counterId, orderRequest.getId());   // 组装ID [  柜台ID,  委托ID ]
            orderRequest.setCounterOid(counterOid);

            //保存委托到缓存
            byte[] serialize = null;
            try {
                //序列化委托订单
                serialize = bodyCodec.serialize(orderRequest);
            } catch (Exception e) {
                log.error("序列化請求失敗: ", e);
            }
            if (serialize == null) {
                return ResponseEnum.FAIL;
            }

            vertx.eventBus().send(ORDER_DATA_CACHE_ADDR, Buffer.buffer(serialize));

            // 4.打包委托(orderRequest --> commonmsg -->tcp数据流)
            // 发送网关
            gatewayUtil.sendOrder(orderRequest);

            log.info("發送訂單請求對象: {}", JSONObject.toJSONString(orderRequest));
            return ResponseEnum.SUCCESS;
        } else {
            return ResponseEnum.USER_BALANCE_SHORTAGE;
        }
    }

    @Override
    public ResponseEnum cancelOrder(OrderRequest orderRequest) {
        long counterOid = idUtil.combineOid(counterId, orderRequest.getId());   // 组装ID [  柜台ID,  委托ID ]
        orderRequest.setCounterOid(counterOid);

        // 发送网关
        gatewayUtil.sendOrder(orderRequest);

        log.info("取消訂單請求對象: {}", JSONObject.toJSONString(orderRequest));
        return ResponseEnum.SUCCESS;
    }
}
