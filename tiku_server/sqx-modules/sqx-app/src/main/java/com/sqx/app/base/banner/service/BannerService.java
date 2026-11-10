package com.sqx.app.base.banner.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.banner.domain.bo.*;
import com.sqx.app.base.banner.domain.vo.AppBannerItemVO;
import com.sqx.app.base.banner.domain.vo.BannerVo;

import java.util.List;

/**
 * @author chengming
 * @since 2025/5/16
 */
public interface BannerService {

    /**
     * 用户端轮播列表（按位置/状态，默认启用中的首页 Banner）
     */
    List<AppBannerItemVO> listAppBanners(Integer position, Integer state);

    void saveInfo(BannerSaveBo bannerSaveBo);

    IPage<BannerVo> pageInfo(BannerPageBo bannerPageBo);

    BannerVo info(BannerInfoBo bannerInfoBo);

    void updateInfo(BannerUpdateBo bannerUpdateBo);

    void delete(BannerInfoBo bannerInfoBo);

    void updateState(BannerUpdateStateBo bannerUpdateStateBo);
}
