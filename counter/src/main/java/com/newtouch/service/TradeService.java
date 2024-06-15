package com.newtouch.service;

import com.newtouch.entity.Trade;

import java.util.List;

public interface TradeService {


    List<Trade> getTradeList(long uid);
}
