package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderCartProductVo;
import com.mmall.vo.OrderVo;
import net.sf.jsqlparser.schema.Server;

import java.util.Map;

public interface IOrderService {

    ServerResponse<Map> pay(Long orderNo, Integer userId, String path);

    ServerResponse alipayCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Long orderNo, Integer userId);

    ServerResponse<OrderVo> createOrder(Integer shippingId, Integer userId);

    ServerResponse<OrderCartProductVo> getOrderCartProduct(Integer userId);

    ServerResponse cancel(Integer userId, Long orderNo);

    ServerResponse<PageInfo<OrderVo>> listOrderVoByUserId(Integer userId, int pageNum, int pageSize);

    ServerResponse<OrderVo> detail(Integer userId, Long orderNo);

    ServerResponse<PageInfo<OrderVo>> listOrderVo(int pageNum, int pageSize);

    ServerResponse<OrderVo> searchByOrderNo(Long orderNo);

    ServerResponse sendGoods(Long orderNo);
}
