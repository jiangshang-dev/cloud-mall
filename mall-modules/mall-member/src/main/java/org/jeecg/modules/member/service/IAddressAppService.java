package org.jeecg.modules.member.service;

import org.jeecg.modules.member.entity.FdUserAddress;

import java.util.List;

public interface IAddressAppService {

    List<FdUserAddress> listByUser(Long userId);

    FdUserAddress saveAddress(Long userId, FdUserAddress address);

    void setDefault(Long userId, Long addressId);

    void deleteAddress(Long userId, Long addressId);
}
