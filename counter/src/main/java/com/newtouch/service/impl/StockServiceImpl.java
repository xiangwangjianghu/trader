package com.newtouch.service.impl;

import com.newtouch.dto.request.StockRequest;
import com.newtouch.entity.Stock;
import com.newtouch.mappers.StockMapper;
import com.newtouch.mappers.UserMapper;
import com.newtouch.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public List<Stock> getStockList(StockRequest stockRequest) {
        return stockMapper.getStockList(stockRequest);
    }
}
