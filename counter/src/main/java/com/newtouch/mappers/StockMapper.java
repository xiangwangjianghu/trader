package com.newtouch.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newtouch.dto.request.StockRequest;
import com.newtouch.entity.Stock;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

public interface StockMapper extends BaseMapper<Stock> {
    List<Stock> getStockList(@Param("stockRequest") StockRequest stockRequest);
}
