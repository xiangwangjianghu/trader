package com.newtouch.dto.market;

import lombok.Builder;
import lombok.Data;

// 一檔行情
@Builder
@Data
public class L1MarketData {
    // 五檔行情
    public static final int L1_SIZE = 5;

    public int code;

    public long newPrice;

    public long timestamp;

    // 買賣實際檔位
    public transient int buySize;
    public transient int sellSize;
    public long[] buyPrices;
    public long[] buyCounts;
    public long[] sellPrices;
    public long[] sellCounts;

    public L1MarketData(long[] buyPrices, long[] buyCounts,
                        long[] sellPrices, long[] sellCounts) {
        this.buyPrices = buyPrices;
        this.buyCounts = buyCounts;

        this.sellPrices = sellPrices;
        this.sellCounts = sellCounts;

        this.buySize = buyPrices.length;
        this.sellSize = sellPrices.length;
    }

    public L1MarketData(int buySize, int sellSize) {
        this.buyPrices = new long[buySize];
        this.buyCounts = new long[buySize];

        this.sellPrices = new long[sellSize];
        this.sellCounts = new long[sellSize];
    }
}
