package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

public interface IProductService {

    ServerResponse<PageInfo> list(Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> search(String productName, Integer productId, Integer pageNum, Integer pageSize);

    ServerResponse<ProductDetailVo> detail(Integer productId);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<String> save(Product product);

}
