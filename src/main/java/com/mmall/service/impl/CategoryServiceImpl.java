package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {

    private static Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse<List<Category>> getCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectByParentId(categoryId);
        if(!CollectionUtils.isEmpty(categoryList)){
            return ServerResponse.createSuccess(categoryList);
        }else {
            logger.info("查询Category不存储，categoryId: " + categoryId);
        }
        return ServerResponse.createErrorWithMsg("未找到该品类");

    }

    @Override
    public ServerResponse<String> addCategory(Integer parentId, String categoryName) {

        int resultCount = categoryMapper.checkCategory(parentId);
        if(resultCount == 0){
            return ServerResponse.createErrorWithMsg("parentId 不存在");
        }

        Category category = new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        int rowCount = categoryMapper.insert(category);
        if(rowCount > 0){
            return ServerResponse.createSuccessWithMsg("添加品类成功");
        }
        return ServerResponse.createErrorWithMsg("添加品类失败");
    }

    @Override
    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName) {

        int resultCount = categoryMapper.checkCategory(categoryId);
        if(resultCount == 0){
            return ServerResponse.createErrorWithMsg("categoryId 不存在");
        }

        Category updateCategory = new Category();
        updateCategory.setId(categoryId);
        updateCategory.setName(categoryName);

        resultCount = categoryMapper.insertSelective(updateCategory);
        if(resultCount > 0){
            return ServerResponse.createSuccessWithMsg("更新品类名字成功");
        }

        return ServerResponse.createErrorWithMsg("更新品类名字失败");
    }

    @Override
    public ServerResponse<List<Integer>> getDeepCategory(Integer categoryId) {

        int resultCount = categoryMapper.checkCategory(categoryId);
        if(resultCount == 0){
            return ServerResponse.createErrorWithMsg("categoryId 不存在");
        }
        List<Integer> result = new ArrayList<Integer>();

        this.getDeepCategory(categoryId, result);

        if(CollectionUtils.isEmpty(result)){
            logger.info("getDeepCategory查询结果为空，categoryId: " + categoryId);
        }

        return ServerResponse.createSuccess(result);
    }

    private void getDeepCategory(Integer categoryId, List<Integer> result){
        List<Category> categoryList = categoryMapper.selectByParentId(categoryId);
        for(Category category: categoryList){
            result.add(category.getId());
            this.getDeepCategory(category.getId());
        }
    }


}
