package com.newtouch.service.impl;

import com.newtouch.entity.Stock;
import com.newtouch.mappers.StockMapper;
import com.newtouch.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper stockMapper;

    @Override
    public IntHashSet getStockList() {
        IntHashSet codeSet = new IntHashSet();

        List<Stock> stockList = stockMapper.getStockList();
        for (Stock stock : stockList) {
            codeSet.add(stock.getCode());
        }

        return codeSet;
    }
}
