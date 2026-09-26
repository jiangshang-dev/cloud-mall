package org.jeecg.modules.member.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.member.entity.FdMallOrder;
import org.jeecg.modules.member.entity.FdMallProduct;

import java.util.List;

public interface IMallAppService {

    List<FdMallProduct> listActiveProducts();

    FdMallProduct getProductDetail(Long id);

    IPage<FdMallOrder> pageUserOrders(Long userId, Integer pageNo, Integer pageSize);
}
