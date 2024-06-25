package com.newtouch.service;

import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.math.BigDecimal;

public interface UserService {
    /**
     * 所有餘額
     *
     * @return User
     * @throws Exception
     */
    LongObjectHashMap<BigDecimal> getBalanceList();
}
