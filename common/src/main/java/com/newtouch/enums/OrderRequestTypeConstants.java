package com.newtouch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderRequestTypeConstants {


    ///////////////委托类//////////////
    //创建订单
    public static final int NEW_ORDER = 0;
    //取消订单
    public static final int CANCEL_ORDER = 1;

    //////权限类(交易所几乎没有用过,都是盘后改，初始化的时候载入新数据)////////
    public static final int SUSPEND_USER = 2;
    public static final int RESUME_USER = 3;

    ///////////////状态类//////////////
    public static final int SHUTDOWN_ENGINE = 4;

    ///////////////查询类//////////////
    public static final int BINARY_DATA = 5;
    public static final int ORDER_BOOK_REQUEST = 6;

    ///////////////行情类//////////////
    public static final int HQ_PUB = 7;


    ///////////////资金类//////////////
    public static final int BALANCE_ADJUSTMENT = 8;
}
