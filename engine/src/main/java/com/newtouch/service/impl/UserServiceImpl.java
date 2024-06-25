package com.newtouch.service.impl;

import com.newtouch.entity.User;
import com.newtouch.mappers.UserMapper;
import com.newtouch.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {


    @Autowired
    private UserMapper userMapper;

    @Override
    public LongObjectHashMap<BigDecimal> getBalanceList() {
        List<User> userList = userMapper.getUserList();

        LongObjectHashMap<BigDecimal> uidBalanceMap = new LongObjectHashMap<>();
        for (User user : userList) {
            uidBalanceMap.put(user.getId(), user.getBalance());
        }

        return uidBalanceMap;
    }
}
