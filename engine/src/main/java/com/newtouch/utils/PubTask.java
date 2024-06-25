package com.newtouch.utils;

import com.newtouch.dto.order.OrderRequest;
import com.newtouch.enums.OrderRequestTypeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.TimerTask;

@Slf4j
@Component
public class PubTask extends TimerTask {

    @Autowired
    private DisruptorUtil disruptorUtil;


    @Override
    public void run() {
        disruptorUtil.submit(OrderRequest.builder().orderRequestType(OrderRequestTypeConstants.HQ_PUB).build());
    }
}
