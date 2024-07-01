package com.newtouch.bus;

import com.newtouch.dto.order.CommonMsg;
import io.vertx.core.Vertx;

public interface IBusSender {
    void startup(Vertx vertx);

    void publish(CommonMsg commonMsg);
}
