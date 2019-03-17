package com.mmall.controller.portal;

import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse login(String username ,String password, HttpSession session){
        ServerResponse<User> response = iUserService.login(username, password, Constant.ROLE.CUSTOM_USER);
        if(response.isSuccess()){
            session.setAttribute(Constant.CURRENT_USER, response.getData());
        }
        return response;
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public void logout(HttpSession session){
        session.removeAttribute(Constant.CURRENT_USER);
    }

    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse register(User user){
        return iUserService.register(user);
    }

    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse checkValid(String str, String type) {
        return iUserService.checkValid(str, type);
    }

    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user){
            return ServerResponse.createErrorWithMsg("用户未登录,无法获取当前用户信息!");
        }
        return ServerResponse.createSuccess(user);
    }

    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.forgetGetQuestion(username);
    }

    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer){
        return iUserService.forgetCheckAnswer(username, question, answer);
    }

    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse forgetResetPassword(String username, String passwordNew, String forgetToken){
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user){
            return ServerResponse.createErrorWithMsg("用户未登录,无法获取当前用户信息!");
        }
        return iUserService.resetPassword(user, passwordOld, passwordNew);
    }

    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> updateInformation(User user, HttpSession session){
        User currentUser = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == currentUser){
            return ServerResponse.createErrorWithMsg("用户未登录,无法获取当前用户信息!");
        }
        user.setId(currentUser.getId());
        ServerResponse response = iUserService.updateInformation(user);
        if(response.isSuccess()){
            return ServerResponse.createSuccessWithMsg("更新个人信息成功");
        }
        return ServerResponse.createError();
    }
}
