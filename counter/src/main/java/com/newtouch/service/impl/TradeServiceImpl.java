package com.newtouch.service.impl;

import com.alibaba.fastjson2.JSON;
import com.newtouch.entity.Order;
import com.newtouch.entity.Trade;
import com.newtouch.enums.CacheEnum;
import com.newtouch.mappers.OrderMapper;
import com.newtouch.mappers.TradeMapper;
import com.newtouch.service.TradeService;
import com.newtouch.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TradeServiceImpl implements TradeService {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private TradeMapper tradeMapper;


    @Override
    public List<Trade> getTradeList(long uid) {
        List<Trade> tradeList = new ArrayList<>();

        String key = String.valueOf(uid);
        String orderCache = redisUtil.getCache(key, CacheEnum.TRADE);

        if (StringUtils.isEmpty(orderCache)) {
            tradeList = tradeMapper.selectListByUid(uid);
            redisUtil.addCache(key, JSON.toJSONString(tradeList), CacheEnum.POSITION);
        } else {
            tradeList = JSON.parseArray(orderCache, Trade.class);
        }

        return tradeList;
    }
}
