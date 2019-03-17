package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.util.FtpUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse<PageInfo> list(Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);

        List<Product> productList = productMapper.selectList();
        List<ProductVo> productVoList = Lists.newArrayList();
        for(Product productItem : productList){
            productVoList.add(this.assembleProductListVo(productItem));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productVoList);

        return ServerResponse.createSuccess(pageInfo);
    }

    @Override
    public ServerResponse<PageInfo> search(String productName, Integer productId, Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        String productSearchName = new StringBuilder().append("%").append(productName).append("%").toString();

        List<Product> productList = productMapper.searchList(productId, productSearchName);
        List<ProductVo> productVoList = Lists.newArrayList();
        for(Product productItem : productList){
            productVoList.add(this.assembleProductListVo(productItem));
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productVoList);

        return ServerResponse.createSuccess(pageInfo);
    }

    @Override
    public ServerResponse<ProductDetailVo> detail(Integer productId) {

        Product product = productMapper.selectByPrimaryKey(productId);

        if(null != product) {
            ProductDetailVo productDetailVo = this.assembleProductDetailVo(product);
            return ServerResponse.createSuccess(productDetailVo);
        }
        return ServerResponse.createError();
    }

    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {

        int resultCount = productMapper.setSaleStatus(productId, status);
        if(resultCount > 0){
            return ServerResponse.createSuccessWithMsg("修改产品状态成功");
        }
        return ServerResponse.createErrorWithMsg("修改产品状态失败");
    }

    @Override
    public ServerResponse<String> save(Product product) {

        int resultCount = productMapper.checkProduct(product.getId());
        if(resultCount > 0){
            resultCount = productMapper.updateByPrimaryKeySelective(product);
            if(resultCount > 0){
                return ServerResponse.createSuccessWithMsg("更新产品成功");
            }
        }else {
            resultCount = productMapper.insertSelective(product);
            if(resultCount > 0){
                return ServerResponse.createSuccessWithMsg("新增产品成功");
            }
        }

        return ServerResponse.createErrorWithMsg("更新产品失败");
    }

    private ProductVo assembleProductListVo(Product product){

        ProductVo productVo = new ProductVo();
        productVo.setId(product.getId());
        productVo.setCategoryId(product.getCategoryId());
        productVo.setMainImage(product.getMainImage());
        productVo.setName(product.getName());
        productVo.setPrice(product.getPrice());
        productVo.setSubtitle(product.getSubtitle());

        return productVo;
    }

    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        productDetailVo.setParentCategoryId(category.getParentId());

        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setImageHost(FtpUtil.ftpServerHttpPrefix);
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setCreateTime(product.getCreateTime());
        productDetailVo.setUpdateTime(product.getUpdateTime());

        return productDetailVo;
    }
}
