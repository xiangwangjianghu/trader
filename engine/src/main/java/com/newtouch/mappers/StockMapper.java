package com.newtouch.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newtouch.entity.Stock;

import java.util.List;

public interface StockMapper extends BaseMapper<Stock> {
    List<Stock> getStockList();
}
