package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.vo.OrderCartProductVo;
import com.mmall.vo.OrderVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
public class OrderController {

    private Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    @RequestMapping(value = "pay.do")
    @ResponseBody
    public ServerResponse<Map> pay(Long orderNo, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        String path = session.getServletContext().getRealPath("upload");
        return iOrderService.pay(orderNo, currentUser.getId(), path);
    }

    @RequestMapping(value = "alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String, String> params = Maps.newHashMap();

        Map requestParams = request.getParameterMap();
        for(Iterator iter = requestParams.keySet().iterator(); iter.hasNext();){
            String name = (String)iter.next();
            String[] values = (String[])requestParams.get(name);
            String valueStr = "";
            for(int i=0; i<values.length; i++){
                valueStr =(1 == (values.length-1))? valueStr + values[i] :valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        logger.info("支付宝回调， sign:{}, trade_status:{}, 参数：{}", params.get("sign"), params.get("trade_status"), params.toString());


        //非常重要,验证回调的正确性,是不是支付宝发的.并且呢还要避免重复通知.
        params.remove("sign_type");

        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params,
                    Configs.getAlipayPublicKey(),"utf-8", Configs.getSignType());

            if(!alipayRSACheckedV2){
                return ServerResponse.createErrorWithMsg("非法请求,验证不通过");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常",e);
        }

        //todo 验证各种数据

        ServerResponse serverResponse = iOrderService.alipayCallback(params);
        if(serverResponse.isSuccess()){
            return Constant.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Constant.AlipayCallback.RESPONSE_FAILED;

    }


    @RequestMapping(value = "query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Map> queryOrderPayStatus(Long orderNo, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iOrderService.queryOrderPayStatus(orderNo, currentUser.getId());
    }


    @RequestMapping(value = "create.do")
    @ResponseBody
    public ServerResponse<OrderVo> create(Integer shippingId, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iOrderService.createOrder(shippingId, currentUser.getId());
    }

    @RequestMapping(value = "get_order_cart_product.do")
    @ResponseBody
    public ServerResponse<OrderCartProductVo> getOrderCartProduct(HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iOrderService.getOrderCartProduct(currentUser.getId());
    }

    @RequestMapping(value = "cancel.do")
    @ResponseBody
    public ServerResponse<OrderVo> cancel(Long orderNo, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iOrderService.cancel(currentUser.getId(), orderNo);
    }

    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse<PageInfo<OrderVo>> list(@RequestParam(defaultValue = "1") int pageNum,
                                         @RequestParam(defaultValue = "10") int pageSize, HttpSession session) {
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iOrderService.listOrderVoByUserId(currentUser.getId(), pageNum, pageSize);
    }


    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> detail(Long orderNo, HttpSession session) {
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iOrderService.detail(currentUser.getId(), orderNo);
    }










}
