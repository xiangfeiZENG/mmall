package com.mmall.controller.backend;

import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping(value = "get_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Category>> getCategory(@RequestParam(value = "categoryId", defaultValue = "String") int categoryId, HttpSession session){

        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCategoryService.getCategory(categoryId);
    }

    @RequestMapping(value = "add_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addCategory(@RequestParam(value = "parentId", defaultValue = "0") Integer parentId,  String categoryName, HttpSession session){

        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCategoryService.addCategory(parentId, categoryName);
    }

    @RequestMapping(value = "set_category_name.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName, HttpSession session){

        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user){
            return ServerResponse.createErrorNeedtLogin();
        }

        return iCategoryService.setCategoryName(categoryId, categoryName);
    }

    @RequestMapping(value = "get_deep_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<Integer>> getDeepCategory(Integer categoryId, HttpSession session){

        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iCategoryService.getDeepCategory(categoryId);
    }


}
