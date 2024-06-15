package com.newtouch.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newtouch.dto.request.LoginRequest;
import com.newtouch.entity.User;
import io.lettuce.core.dynamic.annotation.Param;

import java.math.BigDecimal;

public interface UserMapper extends BaseMapper<User> {
    User getUser(@Param("loginRequest") LoginRequest loginRequest);

    boolean updatePassword(@Param("loginRequest") LoginRequest loginRequest);

    BigDecimal getBalance(@Param("uid") Long uid);

    void updateBalance(@Param("uid") Long uid, @Param("amount") BigDecimal amount);
}
