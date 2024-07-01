package com.newtouch.dto.market;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

// 一檔行情
@Builder
@Data
public class L1MarketData {
    // 五檔行情
    public static final int L1_SIZE = 5;

    public int code;

    public BigDecimal newPrice;

    public long timestamp;

    // 買賣實際檔位
    public transient int buySize;
    public transient int sellSize;
    public BigDecimal[] buyPrices;
    public BigDecimal[] buyCounts;
    public BigDecimal[] sellPrices;
    public BigDecimal[] sellCounts;

    public L1MarketData(BigDecimal[] buyPrices, BigDecimal[] buyCounts,
                        BigDecimal[] sellPrices, BigDecimal[] sellCounts) {
        this.buyPrices = buyPrices;
        this.buyCounts = buyCounts;

        this.sellPrices = sellPrices;
        this.sellCounts = sellCounts;

        this.buySize = buyPrices.length;
        this.sellSize = sellPrices.length;
    }

    public L1MarketData(int buySize, int sellSize) {
        this.buyPrices = new BigDecimal[buySize];
        this.buyCounts = new BigDecimal[buySize];

        this.sellPrices = new BigDecimal[sellSize];
        this.sellCounts = new BigDecimal[sellSize];
    }
}
