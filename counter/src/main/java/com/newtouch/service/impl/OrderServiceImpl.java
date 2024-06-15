package com.newtouch.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.newtouch.dto.request.OrderRequest;
import com.newtouch.entity.Order;
import com.newtouch.enums.CacheEnum;
import com.newtouch.enums.OrderDirectionEnum;
import com.newtouch.enums.ResponseEnum;
import com.newtouch.mappers.OrderMapper;
import com.newtouch.mappers.UserMapper;
import com.newtouch.service.OrderService;
import com.newtouch.utils.IdUtil;
import com.newtouch.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IdUtil idUtil;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${counter.id}")
    private Long counterId;

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

            // 3.序列化委托订单
            long counterOid = idUtil.combineOid(counterId, orderRequest.getId());   // 组装ID [  柜台ID,  委托ID ]
            orderRequest.setCounterOid(counterOid);

            // 4.发送网关


            log.info("訂單請求對象: {}", JSONObject.toJSONString(orderRequest));
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

        log.info("訂單請求對象: {}", JSONObject.toJSONString(orderRequest));
        return ResponseEnum.SUCCESS;
    }
}
