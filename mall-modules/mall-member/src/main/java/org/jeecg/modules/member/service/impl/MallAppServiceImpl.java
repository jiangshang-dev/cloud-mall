package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.jeecg.modules.member.entity.FdMallOrder;
import org.jeecg.modules.member.entity.FdMallProduct;
import org.jeecg.modules.member.service.IFdMallOrderService;
import org.jeecg.modules.member.service.IFdMallProductService;
import org.jeecg.modules.member.service.IMallAppService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MallAppServiceImpl implements IMallAppService {

    @Resource
    private IFdMallProductService productService;
    @Resource
    private IFdMallOrderService orderService;

    @Override
    public List<FdMallProduct> listActiveProducts() {
        LambdaQueryWrapper<FdMallProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMallProduct::getStatus, 1).orderByAsc(FdMallProduct::getSortNo);
        return productService.list(wrapper);
    }

    @Override
    public FdMallProduct getProductDetail(Long id) {
        return productService.getById(id);
    }

    @Override
    public IPage<FdMallOrder> pageUserOrders(Long userId, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdMallOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMallOrder::getUserId, userId).orderByDesc(FdMallOrder::getCreateTime);
        return orderService.page(new Page<>(pageNo, pageSize), wrapper);
    }
}
