package com.newtouch.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newtouch.entity.Trade;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

public interface TradeMapper extends BaseMapper<Trade> {

    List<Trade> selectListByUid(@Param("uid") long uid);
}
