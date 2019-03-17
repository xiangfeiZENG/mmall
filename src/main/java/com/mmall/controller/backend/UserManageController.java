package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/manage/user/")
public class UserManageController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){

        ServerResponse<User> response = iUserService.login(username, password, Constant.ROLE.ADMIN_USER);

        if(response.isSuccess()){
            User user = response.getData();
            user.setPassword(StringUtils.EMPTY);
            session.setAttribute(Constant.CURRENT_USER, user);
            return ServerResponse.createSuccess(user);
        }
        return response;
    }

    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo<User>> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                     @RequestParam(value = "pageSize") Integer pageSize, HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user || !iUserService.checkAdminRole(user)){
            return ServerResponse.createErrorWithMsg("没有权限");
        }
        return iUserService.list(pageNum, pageSize);
    }
}
