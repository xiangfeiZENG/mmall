package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

import java.util.List;

public interface IUserService {

    ServerResponse login(String username, String password, int role);

    ServerResponse register(User user);

    ServerResponse checkValid(String str, String type);

    ServerResponse<String> forgetGetQuestion(String username);

    ServerResponse<String> forgetCheckAnswer(String username, String question, String answer);

    ServerResponse forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew);

    ServerResponse<User> updateInformation(User user);

    Boolean checkAdminRole(User user);

    ServerResponse<PageInfo<User>> list(Integer pageNum, Integer pageSize);
}
