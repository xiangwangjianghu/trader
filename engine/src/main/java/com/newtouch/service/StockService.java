package com.newtouch.service;

import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public interface StockService {
    /**
     * 所有股票数据
     *
     * @return User
     * @throws Exception
     */
    IntHashSet getStockList();
}
