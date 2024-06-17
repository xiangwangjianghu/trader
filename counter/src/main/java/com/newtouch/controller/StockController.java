package com.newtouch.controller;

import com.newtouch.dto.TraderResponse;
import com.newtouch.dto.OrderRequest;
import com.newtouch.entity.Order;
import com.newtouch.entity.Position;
import com.newtouch.entity.Stock;
import com.newtouch.entity.Trade;
import com.newtouch.enums.ResponseEnum;
import com.newtouch.service.OrderService;
import com.newtouch.service.PositionService;
import com.newtouch.service.TradeService;
import com.newtouch.utils.StockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/stock")
public class StockController {

    @Autowired
    private PositionService positionService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StockUtil stockUtil;

    @GetMapping("/getInvertIndex")
    public TraderResponse<Set<Stock>> getInvertIndex(@RequestParam String stockCode) {

        Set<Stock> invertIndex = stockUtil.getInvertIndex(stockCode);

        TraderResponse<Set<Stock>> result = new TraderResponse<>();
        return result.success(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), invertIndex);
    }

    @GetMapping("/getPositionList")
    public TraderResponse<List<Position>> getPositionList(@RequestParam long uid) {

        List<Position> positionList = positionService.getPositionList(uid);

        TraderResponse<List<Position>> result = new TraderResponse<>();
        return result.success(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), positionList);
    }

    @GetMapping("/getOrderList")
    public TraderResponse<List<Order>> getOrderList(@RequestParam long uid) {

        List<Order> orderList = orderService.getOrderList(uid);

        TraderResponse<List<Order>> result = new TraderResponse<>();
        return result.success(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), orderList);
    }

    @GetMapping("/getTradeList")
    public TraderResponse<List<Trade>> getTradeList(@RequestParam long uid) {

        List<Trade> tradeList = tradeService.getTradeList(uid);

        TraderResponse<List<Trade>> result = new TraderResponse<>();
        return result.success(ResponseEnum.SUCCESS.getCode(), ResponseEnum.SUCCESS.getMsg(), tradeList);
    }

    /**
     * 發送訂單
     */
    @GetMapping("/sendOrder")
    public TraderResponse<String> sendOrder(@RequestBody OrderRequest orderRequest) {

        ResponseEnum sendOrderResponse = orderService.sendOrder(orderRequest);

        TraderResponse<String> result = new TraderResponse<>();
        return result.success(sendOrderResponse.getCode(), sendOrderResponse.getMsg(), null);
    }

    /**
     * 取消訂單
     */
    @GetMapping("/cancelOrder")
    public TraderResponse<String> cancelOrder(@RequestBody OrderRequest orderRequest) {

        ResponseEnum cancelOrderResponse = orderService.cancelOrder(orderRequest);

        TraderResponse<String> result = new TraderResponse<>();
        return result.success(cancelOrderResponse.getCode(), cancelOrderResponse.getMsg(), null);
    }

}
