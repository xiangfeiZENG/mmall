package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse<CartVo> list(Integer userId) {

        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createSuccess(cartVo);
    }

    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if(null == productId || null == count){
            ServerResponse.createErrorWithMsg("入参有误");
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if(null == cart){
            Cart cartNew = new Cart();
            cartNew.setUserId(userId);
            cartNew.setProductId(productId);
            cartNew.setQuantity(count);
            cartNew.setChecked(Constant.CART.CHECKED);
            cartMapper.insertSelective(cartNew);
        }else{
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if(null == productId || null == count){
            ServerResponse.createErrorWithMsg("入参有误");
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if(null != cart){
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> delete(Integer userId, String productIds) {
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
            ServerResponse.createErrorWithMsg("入参有误");
        }
        cartMapper.deleteByUserIdAndProductIds(userId, productList);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> unSelect(Integer userId, Integer productId) {
        if(null == productId ){
            ServerResponse.createErrorWithMsg("入参有误");
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if(null != cart){
            cart.setChecked(Constant.CART.UN_CHECKED);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        int count = cartMapper.selectCartProductCount(userId);
        return ServerResponse.createSuccess(count);
    }

    @Override
    public ServerResponse<CartVo> selectAll(Integer userId) {
        cartMapper.checkOrUnCheckAllByUserId(userId, Constant.CART.CHECKED);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> unSelectAll(Integer userId) {
        cartMapper.checkOrUnCheckAllByUserId(userId, Constant.CART.UN_CHECKED);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> select(Integer userId, Integer productId) {
        if(null == productId ){
            ServerResponse.createErrorWithMsg("入参有误");
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if(null != cart){
            cart.setChecked(Constant.CART.CHECKED);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    private CartVo getCartVoLimit(Integer userId){

        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        Boolean allchecked = Boolean.TRUE;
        if(!CollectionUtils.isEmpty(cartList)){
            for(Cart cartItem: cartList){
                if(cartItem.getChecked() != 1)
                    allchecked = Boolean.FALSE;
                cartProductVoList.add(this.assembleCartProductVo(cartItem));
            }
        }else{
            allchecked = Boolean.FALSE;
        }

        CartVo cartVo = new CartVo();
        cartVo.setCartProductVoList(cartProductVoList);
        BigDecimal totalPrice = new BigDecimal("0");
        for(CartProductVo cartProductVoItem : cartProductVoList){
            totalPrice = BigDecimalUtil.add(cartProductVoItem.getProductTotalPrice().doubleValue(), totalPrice.doubleValue());
        }
        cartVo.setCartTotalPrice(totalPrice);
        cartVo.setAllChecked(allchecked);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private CartProductVo assembleCartProductVo(Cart cart){
        CartProductVo cartProductVo = new CartProductVo();
        Product product = productMapper.selectByPrimaryKey(cart.getProductId());

        cartProductVo.setId(cart.getId());
        cartProductVo.setUserId(cart.getUserId());
        cartProductVo.setProductId(cart.getProductId());

        //判断库存
        int buyLimitCount;
        if(product.getStock() >= cart.getQuantity()){
            //库存充足
            buyLimitCount = cart.getQuantity();
            cartProductVo.setLimitQuantity(Constant.CART.LIMIT_NUM_SUCCESS);
        }else {
            buyLimitCount = product.getStock();
            cartProductVo.setLimitQuantity(Constant.CART.LIMIT_NUM_FAIL);

            //购物车中更新有效库存
            Cart cartForQuantity = new Cart();
            cartForQuantity.setId(cart.getId());
            cartForQuantity.setQuantity(buyLimitCount);
            cartMapper.updateByPrimaryKeySelective(cartForQuantity);
        }
        cartProductVo.setQuantity(buyLimitCount);

        cartProductVo.setChecked(cart.getChecked());

        cartProductVo.setProductName(product.getName());
        cartProductVo.setProductSubtitle(product.getSubtitle());
        cartProductVo.setProductMainImage(product.getMainImage());
        cartProductVo.setProductPrice(product.getPrice());

        cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity().doubleValue()));

        cartProductVo.setProductStock(product.getStock());

        return cartProductVo;
    }
}
