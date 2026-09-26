package org.jeecg.modules.cuisine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.cuisine.entity.FdHomeBanner;
import org.jeecg.modules.cuisine.entity.FdRecipe;
import org.jeecg.modules.cuisine.mapper.FdHomeBannerMapper;
import org.jeecg.modules.cuisine.service.IFdHomeBannerService;
import org.jeecg.modules.cuisine.service.IFdRecipeService;
import org.jeecg.modules.cuisine.vo.HomeBannerAdminVO;
import org.jeecg.modules.cuisine.vo.HomeBannerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FdHomeBannerServiceImpl extends ServiceImpl<FdHomeBannerMapper, FdHomeBanner>
        implements IFdHomeBannerService {

    @Resource
    private IFdRecipeService recipeService;

    @Override
    public List<HomeBannerVO> listEnabledBanners() {
        LambdaQueryWrapper<FdHomeBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdHomeBanner::getStatus, 1)
                .orderByAsc(FdHomeBanner::getSortNo)
                .orderByDesc(FdHomeBanner::getCreateTime);
        List<FdHomeBanner> banners = list(wrapper);
        if (banners.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> recipeIds = banners.stream()
                .map(FdHomeBanner::getRecipeId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, FdRecipe> recipeMap = recipeIds.isEmpty()
                ? Collections.emptyMap()
                : recipeService.listByIds(recipeIds).stream()
                .collect(Collectors.toMap(FdRecipe::getId, r -> r, (a, b) -> a));

        return banners.stream().map(banner -> {
            FdRecipe recipe = banner.getRecipeId() != null ? recipeMap.get(banner.getRecipeId()) : null;
            String title = StringUtils.hasText(banner.getTitle())
                    ? banner.getTitle()
                    : (recipe != null ? recipe.getTitle() : null);
            String subtitle = StringUtils.hasText(banner.getSubtitle())
                    ? banner.getSubtitle()
                    : (recipe != null ? recipe.getSubtitle() : null);
            String imageUrl = StringUtils.hasText(banner.getImageUrl())
                    ? banner.getImageUrl()
                    : (recipe != null ? recipe.getCoverImage() : null);
            return HomeBannerVO.builder()
                    .id(banner.getId())
                    .recipeId(banner.getRecipeId())
                    .title(title)
                    .subtitle(subtitle)
                    .imageUrl(imageUrl)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public IPage<HomeBannerAdminVO> pageAdmin(FdHomeBanner query, Map<String, String[]> parameterMap,
                                                Integer pageNo, Integer pageSize) {
        QueryWrapper<FdHomeBanner> queryWrapper = QueryGenerator.initQueryWrapper(query, parameterMap);
        queryWrapper.orderByAsc("sort_no").orderByDesc("create_time");
        Page<FdHomeBanner> page = new Page<>(pageNo, pageSize);
        return page(page, queryWrapper).convert(this::toAdminVO);
    }

    @Override
    public void applyDefaultsForSave(FdHomeBanner banner, boolean isCreate) {
        Date now = new Date();
        if (isCreate && banner.getCreateTime() == null) {
            banner.setCreateTime(now);
        }
        banner.setUpdateTime(now);
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getSortNo() == null) {
            banner.setSortNo(0);
        }
    }

    private HomeBannerAdminVO toAdminVO(FdHomeBanner banner) {
        HomeBannerAdminVO vo = new HomeBannerAdminVO();
        BeanUtils.copyProperties(banner, vo);
        if (banner.getRecipeId() != null) {
            FdRecipe recipe = recipeService.getById(banner.getRecipeId());
            if (recipe != null) {
                vo.setRecipeTitle(recipe.getTitle());
            }
        }
        return vo;
    }
}
