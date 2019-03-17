package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Override
    public ServerResponse<User> login(String username, String password, int role) {
        if(0 == userMapper.checkUsername(username)){
            return ServerResponse.createErrorWithMsg("用户不存在！");
        }

        password = MD5Util.MD5EncodeUtf8(password);

        User loginUser = userMapper.selectLogin(username, password, role);
        if(null == loginUser){
            return ServerResponse.createErrorWithMsg("用户密码不正确！");
        }
        loginUser.setPassword(StringUtils.EMPTY);

        return ServerResponse.createSuccess(loginUser);
    }

    @Override
    public ServerResponse register(User user) {
        int resultCount = userMapper.checkUsername(user.getUsername());
        if(resultCount > 0){
            return ServerResponse.createErrorWithMsg("用户已存在！");
        }
        resultCount = userMapper.checkEmail(user.getEmail());
        if(resultCount > 0){
            return ServerResponse.createErrorWithMsg("邮箱地址已存在!");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        user.setRole(Constant.ROLE.CUSTOM_USER);

        resultCount = userMapper.insert(user);
        if(1 != resultCount){
            return ServerResponse.createError();
        }

        return ServerResponse.createSuccess();
    }

    @Override
    public ServerResponse checkValid(String str, String type) {

        if(Constant.USERNAME.equals(type)){
            if(userMapper.checkUsername(str) > 0){
                return ServerResponse.createSuccessWithMsg("用户已存在！");
            }
        }

        if(Constant.EMAIL.equals(type)){
            if(userMapper.checkEmail(str) >0 ){
                return ServerResponse.createSuccessWithMsg("邮箱地址已存在！");
            }
        }
        return ServerResponse.createSuccessWithMsg("校验成功！");
    }

    @Override
    public ServerResponse<String> forgetGetQuestion(String username) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0){
            return ServerResponse.createErrorWithMsg("用户不存在！");
        }

        String question = userMapper.forgetGetQuestion(username);
        if(StringUtils.isBlank(question)){
            return ServerResponse.createErrorWithMsg("该用户未设置找回密码问题");
        }
        return ServerResponse.createSuccess(question);
    }

    @Override
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {

        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount > 0){
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createSuccess(forgetToken);
        }
        return ServerResponse.createErrorWithMsg("问题答案错误!");
    }

    @Override
    public ServerResponse forgetResetPassword(String username, String passwordNew, String forgetToken) {

        if(StringUtils.equals(TokenCache.getKey(TokenCache.TOKEN_PREFIX + username), forgetToken)){
            int resultCount = userMapper.resetPassword(username,MD5Util.MD5EncodeUtf8(passwordNew));
            if(resultCount > 0 ){
                return ServerResponse.createSuccessWithMsg("修改密码成功");
            } else {
                return ServerResponse.createError();
            }
        }else {
            return ServerResponse.createErrorWithMsg("token已经失效");
        }
    }

    @Override
    public ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew) {
        int resultCount = userMapper.checkPassword(user.getId(), MD5Util.MD5EncodeUtf8(passwordOld));
        if(resultCount > 0){
            resultCount = userMapper.resetPassword(user.getUsername(), MD5Util.MD5EncodeUtf8(passwordNew));
            if(resultCount > 0){
                return ServerResponse.createSuccessWithMsg("修改密码成功");
            }else {
                return  ServerResponse.createError();
            }
        }
        return ServerResponse.createErrorWithMsg("旧密码输入错误");

    }

    public ServerResponse<User> updateInformation(User user){

        int resultCount = userMapper.checkEmail(user.getEmail());
        if(resultCount > 0){
            return ServerResponse.createErrorWithMsg("用户邮箱已被占用");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUsername(user.getUsername());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        resultCount = userMapper.insertSelective(updateUser);
        if(resultCount > 0){
            return ServerResponse.createSuccess(user);
        }

        return ServerResponse.createError();
    }

    @Override
    public Boolean checkAdminRole(User user) {

        if(null != user && Constant.ROLE.ADMIN_USER == user.getRole().intValue()){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public ServerResponse<PageInfo<User>> list(Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);

        List<User> userList = userMapper.list();

        for(User user : userList){
            user.setPassword(StringUtils.EMPTY);
        }

        PageInfo<User> pageInfo = new PageInfo<User>(userList);

        return ServerResponse.createSuccess(pageInfo);



    }


}
