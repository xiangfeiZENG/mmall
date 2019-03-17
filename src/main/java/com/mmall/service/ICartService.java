package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;


public interface ICartService {

    ServerResponse<CartVo> list(Integer userId);

    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count);

    ServerResponse<CartVo> delete(Integer userId, String productIds);

    ServerResponse<CartVo> select(Integer userId, Integer productId);

    ServerResponse<CartVo> unSelect(Integer userId, Integer productId);

    ServerResponse<Integer> getCartProductCount(Integer userId);

    ServerResponse<CartVo> selectAll(Integer userId);

    ServerResponse<CartVo> unSelectAll(Integer userId);




}
