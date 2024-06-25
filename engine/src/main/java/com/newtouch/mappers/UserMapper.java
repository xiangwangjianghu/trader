package com.newtouch.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newtouch.entity.User;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {
    List<User> getUserList();
}
