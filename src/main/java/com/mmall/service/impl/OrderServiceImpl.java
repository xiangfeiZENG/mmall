package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayMonitorService;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayMonitorServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.service.impl.AlipayTradeWithHBServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FtpUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderCartProductVo;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class OrderServiceImpl implements IOrderService {

    private static Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    // 支付宝当面付2.0服务
    private static AlipayTradeService tradeService;

    // 支付宝当面付2.0服务（集成了交易保障接口逻辑）
    private static AlipayTradeService   tradeWithHBService;

    // 支付宝交易保障接口服务，供测试接口api使用，请先阅读readme.txt
    private static AlipayMonitorService monitorService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        // 支付宝当面付2.0服务（集成了交易保障接口逻辑）
        tradeWithHBService = new AlipayTradeWithHBServiceImpl.ClientBuilder().build();

        /** 如果需要在程序中覆盖Configs提供的默认参数, 可以使用ClientBuilder类的setXXX方法修改默认参数 否则使用代码中的默认设置 */
        monitorService = new AlipayMonitorServiceImpl.ClientBuilder()
                .setGatewayUrl("http://mcloudmonitor.com/gateway.do").setCharset("GBK")
                .setFormat("json").build();
    }



    @Override
    public ServerResponse<Map> pay(Long orderNo, Integer userId, String path) {

        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(null == order){
            ServerResponse.createErrorWithMsg("当前用户的订单不存在！");
        }

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = String.valueOf(orderNo);

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("happymmall扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单：").append(outTradeNo).append("共计金额：").append(totalAmount).toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId, orderNo);
        for(OrderItem orderItem : orderItemList){
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if( !folder.exists()){
                    folder.setWritable(Boolean.TRUE);
                    folder.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String qrFilePath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());

                logger.info("filePath:" + qrFilePath);
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrFilePath);

                File targetFile = new File(path, qrFileName);

                try {
                    FtpUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码失败！", e);
                }

                Map<String, String> resultMap = Maps.newHashMap();
                resultMap.put("orderNo", outTradeNo);
                resultMap.put("qrpath", PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName());

                return ServerResponse.createSuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createErrorWithMsg("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createErrorWithMsg("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createErrorWithMsg("不支持的交易状态，交易返回异常!!!");
        }



    }

    @Override
    public ServerResponse alipayCallback(Map<String, String> params) {

        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");


        Order order = orderMapper.selectByOrderNo(orderNo);
        if(null == order){
            return ServerResponse.createErrorWithMsg("非本应用订单！");
        }
        if(order.getStatus() >= Constant.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createSuccessWithMsg("支付宝重复调用");
        }

        if(Constant.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Constant.OrderStatusEnum.PAID.getCode());
            orderMapper.insert(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Constant.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createSuccess();

    }

    @Override
    public ServerResponse queryOrderPayStatus(Long orderNo, Integer userId) {

        Order order = orderMapper.selectByOrderNo(orderNo);
        if(null == order){
            return ServerResponse.createErrorWithMsg("订单不存在");
        }
        if(order.getStatus() >= Constant.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createSuccessWithMsg("true");
        }
        return ServerResponse.createErrorWithMsg("false");
    }

    @Override
    @Transactional
    public ServerResponse<OrderVo> createOrder(Integer shippingId, Integer userId) {

        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);

        //生成订单
        Order order = new Order();

        Long orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setShippingId(shippingId);

        ServerResponse res = this.getOrderItemListByCartList(cartList, userId, orderNo);
        if(! res.isSuccess()) {
            return res;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)res.getData();

        order.setPayment(caculPayment(orderItemList));
        order.setPaymentType(Constant.PayPlatformEnum.ALIPAY.getCode());
        order.setPostage(0);
        order.setStatus(Constant.OrderStatusEnum.NO_PAY.getCode());

        OrderVo orderVo = assembleOrderVo(order, orderItemList);

        orderItemMapper.batchInsert(orderItemList);
        orderMapper.insert(order);

        return ServerResponse.createSuccess(orderVo);

    }

    @Override
    public ServerResponse<OrderCartProductVo> getOrderCartProduct(Integer userId) {

        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);

        ServerResponse res = this.getOrderItemListByCartList(cartList, userId, null);
        if(! res.isSuccess()) {
            return res;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)res.getData();
        BigDecimal productTotalPrice = this.caculPayment(orderItemList);
        List<OrderItemVo> orderItemVoList = assembleOrderItemVoList(orderItemList);

        OrderCartProductVo orderCartProductVo = new OrderCartProductVo();
        orderCartProductVo.setOrderItemVoList(orderItemVoList);
        orderCartProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderCartProductVo.setProductTotalPrice(productTotalPrice);

        return ServerResponse.createSuccess(orderCartProductVo);
    }

    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(null == order){
            return ServerResponse.createErrorWithMsg("用户订单不存在");
        }
        if(order.getStatus() != Constant.OrderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.createErrorWithMsg("用户已付款，无法取消");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Constant.OrderStatusEnum.CANCELED.getCode());

        if(orderMapper.updateByPrimaryKeySelective(updateOrder) > 0) {
            return ServerResponse.createSuccess();
        }
        return ServerResponse.createError();

    }

    @Override
    public ServerResponse<PageInfo<OrderVo>> listOrderVoByUserId(Integer userId, int pageNum, int pageSize) {
        List<Order> orderList = orderMapper.selectByUserId(userId);

        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList) {
            OrderVo orderVo = this.assembleOrderVo(order, null);
            orderVoList.add(orderVo);
        }

        PageHelper.startPage(pageNum, pageSize);
        PageInfo<OrderVo> pageResult = new PageInfo(orderVoList);

        return ServerResponse.createSuccess(pageResult);
    }

    @Override
    public ServerResponse<OrderVo> detail(Integer userId, Long orderNo) {

        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, null);

        return ServerResponse.createSuccess(orderVo);
    }

    @Override
    public ServerResponse<PageInfo<OrderVo>> listOrderVo(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = orderMapper.selectAll();

        List<OrderVo> orderVoList = Lists.newArrayList();
        for(Order order : orderList) {
            OrderVo orderVo = assembleOrderVo(order, null);
            orderVoList.add(orderVo);
        }
        PageInfo<OrderVo> pageInfo = new PageInfo<>(orderVoList);
        return ServerResponse.createSuccess(pageInfo);
    }

    @Override
    public ServerResponse<OrderVo> searchByOrderNo(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(null == order) {
            ServerResponse.createErrorWithMsg("该订单：" + orderNo + "不存在");
        }

        OrderVo orderVo = this.assembleOrderVo(order, null);

        return ServerResponse.createSuccess(orderVo);

    }

    @Override
    public ServerResponse sendGoods(Long orderNo) {

        Order order = orderMapper.selectByOrderNo(orderNo);
        if(null == order) {
            ServerResponse.createErrorWithMsg("该订单：" + orderNo + "不存在");
        }

        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Constant.OrderStatusEnum.SHIPPED.getCode());

        int row = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if(row > 0){
            return ServerResponse.createSuccess("发货成功");
        }
        return ServerResponse.createError();
    }

    private OrderVo assembleOrderVo(Order order , List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null){
            ShippingVo shippingVo = new ShippingVo();
            BeanUtils.copyProperties(shipping, shippingVo);
            orderVo.setShippingVo(shippingVo);
        }

        if(null == orderItemList) {
            orderItemList = orderItemMapper.selectByUserIdAndOrderNo(order.getUserId(), order.getOrderNo());
        }
        List<OrderItemVo> orderItemVoList = assembleOrderItemVoList(orderItemList);

        orderVo.setOrderItemVoList(orderItemVoList);


        return orderVo;
    }

    private BigDecimal caculPayment(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;
    }

    private Long generateOrderNo(){
        return System.currentTimeMillis() + new Random().nextInt(10);
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    private ServerResponse<List<OrderItem>> getOrderItemListByCartList(List<Cart> cartList, Integer userId, Long orderNo) {

        List<OrderItem> orderItemList = Lists.newArrayList();
        for(Cart cartItem : cartList){

            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());

            if(Constant.PROD_STATUS.ON_SALE != product.getStatus()) {
                return ServerResponse.createErrorWithMsg("商品：" + product.getName() + "已下架");
            }
            if(cartItem.getQuantity() > product.getStock()) {
                return ServerResponse.createErrorWithMsg("商品：" + product.getName() + "库存不足");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartItem.getQuantity().doubleValue()));

            orderItemList.add(orderItem);
        }

        return ServerResponse.createSuccess(orderItemList);

    }

    private List<OrderItemVo> assembleOrderItemVoList(List<OrderItem> orderItemList) {
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for(OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = new OrderItemVo();
            BeanUtils.copyProperties(orderItem, orderItemVo);
            orderItemVoList.add(orderItemVo);
        }
        return orderItemVoList;
    }

}
