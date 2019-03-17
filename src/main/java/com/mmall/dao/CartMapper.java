package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    List<Cart> selectByUserId(Integer userId);

    Cart selectByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    int deleteByUserIdAndProductIds(@Param("userId") Integer userId, @Param("productList") List<String> productList);

    int selectCartProductCount(Integer userId);

    int checkOrUnCheckAllByUserId(@Param("userId") Integer userId, @Param("checkStatus") Integer checkStatus);

    List<Cart> selectCheckedCartByUserId(Integer userId);
}