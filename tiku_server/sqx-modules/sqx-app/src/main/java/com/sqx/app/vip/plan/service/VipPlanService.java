package com.sqx.app.vip.plan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.vip.plan.domain.bo.VipPlanInfoBo;
import com.sqx.app.vip.plan.domain.bo.VipPlanPageBo;
import com.sqx.app.vip.plan.domain.bo.VipPlanSaveBo;
import com.sqx.app.vip.plan.domain.bo.VipPlanUpdateBo;
import com.sqx.app.vip.plan.domain.vo.AppVipPlanItemVO;
import com.sqx.app.vip.plan.domain.vo.VipPlanVo;

import java.util.List;

/**
 * 会员套餐	 服务类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:51:13
 */
public interface VipPlanService {

    void saveInfo(VipPlanSaveBo vipPlanSaveBo);

    IPage<VipPlanVo> pageInfo(VipPlanPageBo vipPlanPageBo);

    VipPlanVo info(VipPlanInfoBo vipPlanInfoBo);

    void updateInfo(VipPlanUpdateBo vipPlanUpdateBo);

    void delete(VipPlanInfoBo vipPlanInfoBo);

    List<VipPlanVo> list();

    /**
     * 用户端启用套餐列表（GET /app/vipPlan）
     */
    List<AppVipPlanItemVO> listAppPlans();

}
