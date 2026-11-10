package com.sqx.app.vip.rights.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.vip.rights.domain.bo.VipRightsInfoBo;
import com.sqx.app.vip.rights.domain.bo.VipRightsPageBo;
import com.sqx.app.vip.rights.domain.bo.VipRightsSaveBo;
import com.sqx.app.vip.rights.domain.bo.VipRightsUpdateBo;
import com.sqx.app.vip.rights.domain.vo.AppVipRightsCompareItemVO;
import com.sqx.app.vip.rights.domain.vo.VipRightsVo;

import java.util.List;

/**
 * 会员特权 服务类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:56:11
 */
public interface VipRightsService {

    void saveInfo(VipRightsSaveBo vipRightsSaveBo);

    IPage<VipRightsVo> pageInfo(VipRightsPageBo vipRightsPageBo);

    VipRightsVo info(VipRightsInfoBo vipRightsInfoBo);

    void updateInfo(VipRightsUpdateBo vipRightsUpdateBo);

    void delete(VipRightsInfoBo vipRightsInfoBo);

    List<VipRightsVo> list();

    /**
     * 会员权益对比表（GET /app/vipRights）
     */
    List<AppVipRightsCompareItemVO> listCompareItems();

}
