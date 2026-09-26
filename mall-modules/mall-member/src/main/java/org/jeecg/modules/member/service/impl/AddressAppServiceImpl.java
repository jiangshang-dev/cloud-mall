package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.member.entity.FdUserAddress;
import org.jeecg.modules.member.service.IFdUserAddressService;
import org.jeecg.modules.member.service.IAddressAppService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressAppServiceImpl implements IAddressAppService {

    @Resource
    private IFdUserAddressService addressService;

    @Override
    public List<FdUserAddress> listByUser(Long userId) {
        LambdaQueryWrapper<FdUserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdUserAddress::getUserId, userId)
                .orderByDesc(FdUserAddress::getIsDefault)
                .orderByDesc(FdUserAddress::getUpdateTime);
        return addressService.list(wrapper);
    }

    @Override
    public FdUserAddress saveAddress(Long userId, FdUserAddress address) {
        long now = System.currentTimeMillis();
        address.setUserId(userId);
        address.setUpdateTime(now);
        if (address.getId() == null) {
            address.setCreateTime(now);
        }
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            clearDefault(userId);
        }
        addressService.saveOrUpdate(address);
        return address;
    }

    @Override
    public void setDefault(Long userId, Long addressId) {
        FdUserAddress address = addressService.getById(addressId);
        if (address == null || !userId.equals(address.getUserId())) {
            throw new JeecgBootException("地址不存在");
        }
        clearDefault(userId);
        address.setIsDefault(1);
        address.setUpdateTime(System.currentTimeMillis());
        addressService.updateById(address);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        FdUserAddress address = addressService.getById(addressId);
        if (address == null || !userId.equals(address.getUserId())) {
            throw new JeecgBootException("地址不存在");
        }
        addressService.removeById(addressId);
    }

    private void clearDefault(Long userId) {
        LambdaQueryWrapper<FdUserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdUserAddress::getUserId, userId).eq(FdUserAddress::getIsDefault, 1);
        List<FdUserAddress> list = addressService.list(wrapper);
        for (FdUserAddress item : list) {
            item.setIsDefault(0);
            addressService.updateById(item);
        }
    }
}
