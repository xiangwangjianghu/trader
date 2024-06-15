package com.newtouch.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.newtouch.entity.Stock;
import com.newtouch.mappers.StockMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class StockUtil {

    @Autowired
    private StockMapper stockMapper;

    private HashMultimap<String, Stock> invertIndex = HashMultimap.create();

    public Set<Stock> getInvertIndex(String stockCode) {
        return invertIndex.get(stockCode);
    }

    @PostConstruct
    private void createInvertIndex() {
        log.info("加载股票代码倒排索引开始");
        long start = System.currentTimeMillis();

        List<Stock> stockList = stockMapper.getStockList(null);
        for (Stock stock : stockList) {
            List<String> combineList = combineCode(String.format("%06d", stock.getCode()));
            combineList.addAll(combineCode(stock.getAbbrName()));

            // 加入invertIndex
            for (String stockCode : combineList) {
                if (invertIndex.get(stockCode).size() <= 10)
                    invertIndex.put(stockCode, stock);
            }
        }

        log.info("加载股票代码倒排索引结束, 共计花费: {} 毫秒", System.currentTimeMillis() - start);
    }

    private static List<String> combineCode(String code) {
        ArrayList<String> combineCodeList = Lists.newArrayList();

        int len = code.length();
        for (int i = 0; i < len; i++)
            for (int j = i + 1; j < len + 1; j++)
                combineCodeList.add(code.substring(i, j));

        return combineCodeList;
    }
}
