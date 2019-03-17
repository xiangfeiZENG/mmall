package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.Constant;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.FtpUtil;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    IProductService iProductService;
    @Autowired
    IUserService iUserService;
    @Autowired
    IFileService iFileService;

    @RequestMapping(value = "list.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize, HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iProductService.list(pageNum, pageSize);
    }

    @RequestMapping(value = "search.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<PageInfo> search(String productName, Integer productId,
                                           @RequestParam(value = "pageNum", defaultValue = "1")Integer pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                           HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user){
            return ServerResponse.createErrorNeedtLogin();
        }
        return iProductService.search(productName, productId, pageNum, pageSize);
    }

    @RequestMapping(value = "upload.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Map> upload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user){
            return ServerResponse.createErrorNeedtLogin();
        }

        if(iUserService.checkAdminRole(user)){
            String path = session.getServletContext().getRealPath("upload");
            Map<String, String> map = Maps.newHashMap();
            String uri = iFileService.upload(file, path);
            map.put("uri", uri);
            map.put("url", FtpUtil.ftpServerHttpPrefix + uri);
            return ServerResponse.createSuccess(map);
        }
        return ServerResponse.createErrorWithMsg("不是管理员，无权限操作！");
    }

    @RequestMapping(value = "detail.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId, HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user || !iUserService.checkAdminRole(user)){
            return ServerResponse.createErrorWithMsg("没有权限");
        }
        return iProductService.detail(productId);
    }

    @RequestMapping(value = "set_sale_status.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status, HttpSession session){

        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user || !iUserService.checkAdminRole(user)){
            return ServerResponse.createErrorWithMsg("没有权限");
        }
        return iProductService.setSaleStatus(productId, status);
    }

    @RequestMapping(value = "save.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> save(Product product, HttpSession session){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);
        if(null == user || !iUserService.checkAdminRole(user)){
            return ServerResponse.createErrorWithMsg("没有权限");
        }
        return iProductService.save(product);
    }

    @RequestMapping(value = "richtext_img_upload.do", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> richtextImgUpload(@RequestParam(value = "upload_file", required = false) MultipartFile file, HttpSession session, HttpServletResponse response){
        User user = (User)session.getAttribute(Constant.CURRENT_USER);

        HashMap<String, String> resultMap = Maps.newHashMap();
        if(null == user){
            resultMap.put("success", "false");
            resultMap.put("msg", "请登陆管理员");
            return resultMap;
        }

        if(iUserService.checkAdminRole(user)){
            String path = session.getServletContext().getRealPath("upload");
            Map<String, String> map = Maps.newHashMap();
            String uri = iFileService.upload(file, path);
            resultMap.put("file_path", FtpUtil.ftpServerHttpPrefix + uri);
            resultMap.put("msg", "上传成功");
            resultMap.put("success", "true");
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }
        resultMap.put("success", "false");
        resultMap.put("msg", "error msg");
        return resultMap;
    }





}

