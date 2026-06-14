package com.takeout.module.user.service;

import com.takeout.module.user.dto.AddressDTO;
import com.takeout.module.user.vo.AddressVO;

import java.util.List;

public interface AddressService {

    List<AddressVO> list(Long customerId);

    AddressVO add(Long customerId, AddressDTO dto);

    AddressVO update(Long customerId, Long addressId, AddressDTO dto);

    void delete(Long customerId, Long addressId);

    void setDefault(Long customerId, Long addressId);
}
