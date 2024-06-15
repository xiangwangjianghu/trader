package com.newtouch.service;

import com.newtouch.dto.request.LoginRequest;
import com.newtouch.dto.request.StockRequest;
import com.newtouch.entity.Stock;

import java.util.List;

public interface StockService {
    /**
     * 所有股票数据
     *
     * @param stockRequest
     * @return User
     * @throws Exception
     */
    List<Stock> getStockList(StockRequest stockRequest);

}
