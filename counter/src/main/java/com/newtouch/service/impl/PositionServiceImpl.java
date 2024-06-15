package com.newtouch.service.impl;

import com.alibaba.fastjson2.JSON;
import com.newtouch.entity.Position;
import com.newtouch.entity.Stock;
import com.newtouch.enums.CacheEnum;
import com.newtouch.mappers.PositionMapper;
import com.newtouch.service.PositionService;
import com.newtouch.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PositionServiceImpl implements PositionService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private PositionMapper positionMapper;

    @Override
    public List<Position> getPositionList(long uid) {
        List<Position> positionList = new ArrayList<>();

        String key = String.valueOf(uid);
        String positionCache = redisUtil.getCache(key, CacheEnum.POSITION);

        if (StringUtils.isEmpty(positionCache)) {
            positionList = positionMapper.selectListByUid(uid);
            redisUtil.addCache(key, JSON.toJSONString(positionList), CacheEnum.POSITION);
        } else {
            positionList = JSON.parseArray(positionCache, Position.class);
        }

        return positionList;
    }
}
