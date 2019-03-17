package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

import java.util.Map;

public interface IShippingService {

    ServerResponse<Map> add(Shipping shipping);

    ServerResponse<Shipping> select(Integer shippingId, Integer userId);

    ServerResponse del(Integer userId, Integer shippingId);

    ServerResponse update(Shipping shipping);

    ServerResponse<PageInfo<Shipping>> list(Integer userId, Integer pageNum, Integer pageSize);
}
