package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/shipping/")
public class ShippingController {

    @Autowired
    private IShippingService iShippingService;

    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Map> add(Shipping shipping, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }

        shipping.setUserId(currentUser.getId());
        return iShippingService.add(shipping);
    }

    @RequestMapping(value = "del.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse del(Integer shippingId, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }

        return iShippingService.del(currentUser.getId(), shippingId);

    }

    @RequestMapping(value = "select.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Shipping> select(Integer shippingId, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iShippingService.select(currentUser.getId(), shippingId);
    }

    @RequestMapping(value = "update.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse update(Shipping shipping, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        shipping.setUserId(currentUser.getId());
        return iShippingService.update(shipping);
    }

    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo<Shipping>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iShippingService.list(currentUser.getId(), pageNum, pageSize);
    }



}
