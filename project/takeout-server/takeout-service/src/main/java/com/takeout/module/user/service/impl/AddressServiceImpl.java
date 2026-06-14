package com.takeout.module.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.takeout.common.constant.CommonConstant;
import com.takeout.common.exception.BusinessException;
import com.takeout.module.user.dto.AddressDTO;
import com.takeout.module.user.entity.Address;
import com.takeout.module.user.entity.Customer;
import com.takeout.module.user.mapper.AddressMapper;
import com.takeout.module.user.mapper.CustomerMapper;
import com.takeout.module.user.service.AddressService;
import com.takeout.module.user.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;
    private final CustomerMapper customerMapper;

    @Override
    public List<AddressVO> list(Long customerId) {
        List<Address> addresses = addressMapper.selectList(
                new LambdaQueryWrapper<Address>().eq(Address::getCustomerId, customerId));
        return addresses.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressVO add(Long customerId, AddressDTO dto) {
        // 如果设为默认，先取消其他默认
        if (dto.getIsDefault() == 1) {
            addressMapper.clearDefault(customerId);
        }

        Address addr = new Address();
        addr.setCustomerId(customerId);
        BeanUtil.copyProperties(dto, addr);
        addressMapper.insert(addr);

        // 如果是第一个地址，自动设为默认
        Long count = addressMapper.selectCount(
                new LambdaQueryWrapper<Address>().eq(Address::getCustomerId, customerId));
        if (count == 1) {
            addr.setIsDefault(1);
            addressMapper.updateById(addr);
            // 更新顾客默认地址
            Customer customer = customerMapper.selectById(customerId);
            if (customer != null) {
                customer.setDefaultAddressId(addr.getId());
                customerMapper.updateById(customer);
            }
        }

        return toVO(addr);
    }

    @Override
    @Transactional
    public AddressVO update(Long customerId, Long addressId, AddressDTO dto) {
        Address addr = addressMapper.selectById(addressId);
        if (addr == null || !addr.getCustomerId().equals(customerId)) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "地址不存在");
        }

        if (dto.getIsDefault() == 1) {
            addressMapper.clearDefault(customerId);
        }

        BeanUtil.copyProperties(dto, addr);
        addr.setCustomerId(customerId);
        addressMapper.updateById(addr);
        return toVO(addr);
    }

    @Override
    public void delete(Long customerId, Long addressId) {
        Address addr = addressMapper.selectById(addressId);
        if (addr == null || !addr.getCustomerId().equals(customerId)) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "地址不存在");
        }
        addressMapper.deleteById(addressId);
    }

    @Override
    @Transactional
    public void setDefault(Long customerId, Long addressId) {
        Address addr = addressMapper.selectById(addressId);
        if (addr == null || !addr.getCustomerId().equals(customerId)) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "地址不存在");
        }

        addressMapper.clearDefault(customerId);
        addr.setIsDefault(1);
        addressMapper.updateById(addr);

        // 更新顾客默认地址
        Customer customer = customerMapper.selectById(customerId);
        if (customer != null) {
            customer.setDefaultAddressId(addressId);
            customerMapper.updateById(customer);
        }
    }

    private AddressVO toVO(Address addr) {
        AddressVO vo = new AddressVO();
        BeanUtil.copyProperties(addr, vo);
        vo.setFullAddress(String.format("%s%s%s%s",
                addr.getProvince(), addr.getCity(), addr.getDistrict(), addr.getDetail()));
        return vo;
    }
}
