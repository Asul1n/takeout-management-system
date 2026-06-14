package com.takeout.module.merchant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.module.merchant.dto.MerchantApplyDTO;
import com.takeout.module.merchant.dto.MerchantUpdateDTO;
import com.takeout.module.merchant.vo.MerchantVO;

public interface MerchantService {

    /** 商家入驻申请 */
    void apply(Long userId, MerchantApplyDTO dto);

    /** 管理员：待审核商家列表 */
    Page<MerchantVO> auditList(Integer page, Integer size);

    /** 管理员：审核商家 */
    void audit(Long merchantId, String auditStatus, String reason);

    /** 商家：修改自身信息 */
    MerchantVO updateInfo(Long merchantId, MerchantUpdateDTO dto);

    /** 商家：切换营业状态 */
    void toggleBizStatus(Long merchantId, String bizStatus);

    /** 浏览商家列表 */
    Page<MerchantVO> list(String keyword, Integer page, Integer size);

    /** 商家详情 */
    MerchantVO detail(Long merchantId);
}
