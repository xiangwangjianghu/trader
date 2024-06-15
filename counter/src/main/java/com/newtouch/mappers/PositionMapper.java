package com.newtouch.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.newtouch.entity.Position;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

public interface PositionMapper extends BaseMapper<Position> {
    List<Position> selectListByUid(@Param("uid") long uid);
}
