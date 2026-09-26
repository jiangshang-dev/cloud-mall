package org.jeecg.modules.cuisine.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.cuisine.entity.FdHomeBanner;
import org.jeecg.modules.cuisine.vo.HomeBannerAdminVO;
import org.jeecg.modules.cuisine.vo.HomeBannerVO;

import java.util.List;
import java.util.Map;

public interface IFdHomeBannerService extends IService<FdHomeBanner> {

    List<HomeBannerVO> listEnabledBanners();

    IPage<HomeBannerAdminVO> pageAdmin(FdHomeBanner query, Map<String, String[]> parameterMap,
                                       Integer pageNo, Integer pageSize);

    void applyDefaultsForSave(FdHomeBanner banner, boolean isCreate);
}
