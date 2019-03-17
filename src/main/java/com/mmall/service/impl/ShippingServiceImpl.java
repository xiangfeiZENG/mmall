package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse<Map> add(Shipping shipping) {
        int resultCount = shippingMapper.insert(shipping);
        if(resultCount > 0){
            Map<String, String> map = Maps.newHashMap();
            map.put("shippingId", shipping.getId().toString());
            ServerResponse.createSuccess(map);
        }
        return ServerResponse.createErrorWithMsg("新建地址失败");
    }

    @Override
    public ServerResponse<Shipping> select(Integer shippingId, Integer userId) {
        Shipping shipping = shippingMapper.selectByUserIdAndShippingId(shippingId, userId);
        if(null == shipping){
            return ServerResponse.createErrorWithMsg("查询收获地址失败！");
        }
        return ServerResponse.createSuccess(shipping);

    }

    @Override
    public ServerResponse del(Integer userId, Integer shippingId) {
        int resultCount = shippingMapper.deleteByShippingIdAndUserId(userId, shippingId);
        if(resultCount > 0 ){
            return ServerResponse.createSuccessWithMsg("删除地址成功");
        }
        return ServerResponse.createErrorWithMsg("删除地址失败");
    }

    @Override
    public ServerResponse update(Shipping shipping) {
        int resultCount = shippingMapper.updateByPrimaryKeySelective(shipping);
        if(resultCount > 0){
            ServerResponse.createSuccessWithMsg("更新地址成功");
        }
        return ServerResponse.createErrorWithMsg("更新地址失败");
    }

    @Override
    public ServerResponse<PageInfo<Shipping>> list(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);

        PageInfo<Shipping> pageInfo = new PageInfo<>(shippingList);
        return ServerResponse.createSuccess(pageInfo);
    }
}
