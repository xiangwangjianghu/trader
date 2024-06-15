package com.newtouch.service;

import com.newtouch.entity.Position;

import java.util.List;

public interface PositionService {

    List<Position> getPositionList(long uid);

}
