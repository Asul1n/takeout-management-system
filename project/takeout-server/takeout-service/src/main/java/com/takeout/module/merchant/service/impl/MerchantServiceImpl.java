package com.takeout.module.merchant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.constant.CommonConstant;
import com.takeout.common.exception.BusinessException;
import com.takeout.module.merchant.dto.MerchantApplyDTO;
import com.takeout.module.merchant.dto.MerchantUpdateDTO;
import com.takeout.module.merchant.entity.Merchant;
import com.takeout.module.merchant.mapper.MerchantMapper;
import com.takeout.module.merchant.service.MerchantService;
import com.takeout.module.merchant.vo.MerchantVO;
import com.takeout.module.user.entity.User;
import com.takeout.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void apply(Long userId, MerchantApplyDTO dto) {
        Merchant merchant = merchantMapper.selectById(userId);
        if (merchant == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "商家不存在");
        }
        // 更新商家信息
        BeanUtil.copyProperties(dto, merchant);
        merchant.setAuditStatus("待审核");
        merchantMapper.updateById(merchant);
        log.info("商家入驻申请更新: merchantId={}, name={}", userId, dto.getName());
    }

    @Override
    public Page<MerchantVO> auditList(Integer page, Integer size) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getAuditStatus, "待审核")
                .orderByAsc(Merchant::getCreateTime);
        Page<Merchant> result = merchantMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<MerchantVO>) result.convert(this::toVO);
    }

    @Override
    @Transactional
    public void audit(Long merchantId, String auditStatus, String reason) {
        jdbcTemplate.update("CALL sp_audit_merchant(?,?)", merchantId, auditStatus);
}

    @Override
    public MerchantVO updateInfo(Long merchantId, MerchantUpdateDTO dto) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "商家不存在");
        }
        if (dto.getProvince() != null) merchant.setProvince(dto.getProvince());
        if (dto.getCity() != null) merchant.setCity(dto.getCity());
        if (dto.getDistrict() != null) merchant.setDistrict(dto.getDistrict());
        if (dto.getAddressDetail() != null) merchant.setAddressDetail(dto.getAddressDetail());
        if (dto.getOpenTime() != null) merchant.setOpenTime(dto.getOpenTime());
        if (dto.getCloseTime() != null) merchant.setCloseTime(dto.getCloseTime());
        if (dto.getNotice() != null) merchant.setNotice(dto.getNotice());
        if (dto.getAutoAccept() != null) merchant.setAutoAccept(dto.getAutoAccept());
        merchantMapper.updateById(merchant);
        return toVO(merchant);
    }

    @Override
    public void toggleBizStatus(Long merchantId, String bizStatus) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "商家不存在");
        }
        merchant.setBizStatus(bizStatus);
        merchantMapper.updateById(merchant);
    }

    @Override
    public Page<MerchantVO> list(String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getAuditStatus, "已通过");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Merchant::getName, keyword);
        }
        wrapper.orderByDesc(Merchant::getMonthlySales);
        Page<Merchant> result = merchantMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<MerchantVO>) result.convert(this::toVO);
    }

    @Override
    public MerchantVO detail(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "商家不存在");
        }
        return toVO(merchant);
    }

    private MerchantVO toVO(Merchant m) {
        MerchantVO vo = new MerchantVO();
        BeanUtil.copyProperties(m, vo);
        return vo;
    }
}
