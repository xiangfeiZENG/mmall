package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/order")
public class OrderManageController {

    @Autowired
    IOrderService orderService;

    @RequestMapping("/list.do")
    @ResponseBody
    public ServerResponse<PageInfo<OrderVo>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize, HttpSession session) {
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user || !user.getRole().equals(Constant.ROLE.ADMIN_USER)) {
            return ServerResponse.createErrorWithMsg("请先使用管理员账号登陆");
        }

        return orderService.listOrderVo(pageNum, pageSize);
    }

    @RequestMapping("/search.do")
    @ResponseBody
    public ServerResponse<OrderVo> search(Long orderNo, HttpSession session) {
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user || !user.getRole().equals(Constant.ROLE.ADMIN_USER)) {
            return ServerResponse.createErrorWithMsg("请先使用管理员账号登陆");
        }

        return orderService.searchByOrderNo(orderNo);
    }

    @RequestMapping("/send_goods.do")
    @ResponseBody
    public ServerResponse sendGoods(Long orderNo, HttpSession session) {
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user || !user.getRole().equals(Constant.ROLE.ADMIN_USER)) {
            return ServerResponse.createErrorWithMsg("请先使用管理员账号登陆");
        }

        return orderService.sendGoods(orderNo);
    }
}
