package com.newtouch.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newtouch.dto.order.OrderRequest;
import com.newtouch.entity.Order;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

public interface OrderMapper extends BaseMapper<Order> {
    List<Order> selectListByUid(@Param("uid") long uid);

    void sendOrder(@Param("orderRequest") OrderRequest orderRequest);
}
