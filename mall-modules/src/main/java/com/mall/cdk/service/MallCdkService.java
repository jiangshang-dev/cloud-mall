package com.mall.cdk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.api.dto.CdkVerifyVO;
import com.mall.cdk.entity.MallCdk;
import com.mall.cdk.mapper.MallCdkMapper;
import com.mall.common.constant.MallCdkStatus;
import com.mall.product.entity.MallProduct;
import com.mall.product.mapper.MallProductMapper;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MallCdkService extends ServiceImpl<MallCdkMapper, MallCdk> {

    private static final char[] CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private final SecureRandom random = new SecureRandom();
    private final MallProductMapper productMapper;

    public CdkVerifyVO verify(String rawCode) {
        String code = normalize(rawCode);
        MallCdk cdk = getByCode(code);
        if (cdk == null) {
            throw new JeecgBootException("卡密不存在或错误");
        }
        if (MallCdkStatus.USED.equals(cdk.getStatus())) {
            throw new JeecgBootException("卡密已使用");
        }
        if (MallCdkStatus.DISABLED.equals(cdk.getStatus())) {
            throw new JeecgBootException("卡密已禁用");
        }
        if (cdk.getExpireTime() != null && cdk.getExpireTime().before(new Date())) {
            throw new JeecgBootException("卡密已过期");
        }
        if (!MallCdkStatus.UNUSED.equals(cdk.getStatus())
                && !MallCdkStatus.RESERVED.equals(cdk.getStatus())) {
            throw new JeecgBootException("卡密不可用");
        }
        MallProduct product = productMapper.selectById(cdk.getProductId());
        if (product == null || !"ON".equals(product.getStatus())) {
            throw new JeecgBootException("卡密对应商品不可用");
        }
        return new CdkVerifyVO()
                .setCode(code)
                .setProductId(product.getId())
                .setProductName(product.getName())
                .setProductType(product.getProductType())
                .setCategory(product.getCategory())
                .setPrice(product.getPrice())
                .setDurationDays(product.getDurationDays())
                .setMessage("卡密有效，系统将按套餐自动匹配渠道");
    }

    public MallCdk getByCode(String code) {
        return getOne(new LambdaQueryWrapper<MallCdk>().eq(MallCdk::getCode, code), false);
    }

    public int occupy(String code, String userId) {
        return baseMapper.occupy(code, userId);
    }

    public int reserveById(String id, String userId) {
        return baseMapper.reserveById(id, userId);
    }

    /** 取一张未售库存卡密 */
    public MallCdk pickUnused(String productId) {
        return getOne(new LambdaQueryWrapper<MallCdk>()
                .eq(MallCdk::getProductId, productId)
                .eq(MallCdk::getStatus, MallCdkStatus.UNUSED)
                .and(w -> w.isNull(MallCdk::getExpireTime).or().gt(MallCdk::getExpireTime, new Date()))
                .orderByAsc(MallCdk::getCreateTime)
                .last("LIMIT 1"), false);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<String> batchCreate(String productId, int count, int expireDays) {
        MallProduct product = productMapper.selectById(productId);
        if (product == null) {
            throw new JeecgBootException("商品不存在");
        }
        if (count < 1 || count > 500) {
            throw new JeecgBootException("生成数量需在 1~500");
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, Math.max(expireDays, 1));
        Date expire = cal.getTime();
        String prefix = product.getProductType() == null ? "CDK" : product.getProductType().toUpperCase();
        List<String> codes = new ArrayList<>();
        Date now = new Date();
        for (int i = 0; i < count; i++) {
            String code = prefix + "-" + randomSegment(4) + "-" + randomSegment(4) + "-" + randomSegment(4);
            MallCdk cdk = new MallCdk()
                    .setCode(code)
                    .setProductId(productId)
                    .setStatus(MallCdkStatus.UNUSED)
                    .setExpireTime(expire)
                    .setCreateTime(now);
            save(cdk);
            codes.add(code);
        }
        return codes;
    }

    public static String normalize(String raw) {
        if (oConvertUtils.isEmpty(raw)) {
            throw new JeecgBootException("请输入卡密");
        }
        return raw.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private String randomSegment(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CODE_CHARS[random.nextInt(CODE_CHARS.length)]);
        }
        return sb.toString();
    }
}
