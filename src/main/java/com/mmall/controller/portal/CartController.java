package com.mmall.controller.portal;

import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCartService.list(currentUser.getId());
    }

    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> add(Integer productId, Integer count, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }

        return iCartService.add(currentUser.getId(), productId, count);
    }

    @RequestMapping(value = "update.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> update(Integer productId, Integer count, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCartService.update(currentUser.getId(), productId, count);
    }

    @RequestMapping(value = "delete_product.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> deleteProduct(String productIds, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCartService.delete(currentUser.getId(), productIds);
    }


    @RequestMapping(value = "select.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> select(Integer productId, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCartService.select(currentUser.getId(), productId);
    }

    @RequestMapping(value = "un_select.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> unSelect(Integer productId, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCartService.unSelect(currentUser.getId(), productId);
    }

    @RequestMapping(value = "get_cart_product_count.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Integer> getCartProductCount(HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCartService.getCartProductCount(currentUser.getId());
    }

    @RequestMapping(value = "select_all.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCartService.selectAll(currentUser.getId());
    }

    @RequestMapping(value = "un_select_all.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCartService.unSelectAll(currentUser.getId());
    }

}
