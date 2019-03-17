package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

import java.util.List;

public interface ICategoryService {

    ServerResponse getCategory(Integer categoryId);

    ServerResponse<String> addCategory(Integer parentId, String categoryName);

    ServerResponse<String> setCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Integer>> getDeepCategory(Integer categoryId);


}