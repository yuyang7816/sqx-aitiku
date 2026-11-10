package com.sqx.app.vip.rights.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.ai.support.AiConfigReader;
import com.sqx.app.base.ocr.support.OcrConfigReader;
import com.sqx.app.vip.rights.db.service.IVipRightsService;
import com.sqx.app.vip.rights.domain.bo.VipRightsInfoBo;
import com.sqx.app.vip.rights.domain.bo.VipRightsPageBo;
import com.sqx.app.vip.rights.domain.bo.VipRightsSaveBo;
import com.sqx.app.vip.rights.domain.bo.VipRightsUpdateBo;
import com.sqx.app.vip.rights.domain.entity.VipRights;
import com.sqx.app.vip.rights.domain.vo.AppVipRightsCompareItemVO;
import com.sqx.app.vip.rights.domain.vo.VipRightsVo;
import com.sqx.app.vip.rights.service.VipRightsService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 会员特权 服务实现类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:56:11
 */
@Service
public class VipRightsServiceImpl implements VipRightsService {

    private static final String RIGHTS_CODE_AI = "ai";
    private static final String RIGHTS_CODE_OCR = "ocr";

    @Resource
    private IVipRightsService iVipRightsService;

    @Resource
    private AiConfigReader aiConfigReader;

    @Resource
    private OcrConfigReader ocrConfigReader;

    @Override
    public void saveInfo(VipRightsSaveBo vipRightsSaveBo) {
        VipRights vipRights = new VipRights();
        BeanUtils.copyProperties(vipRightsSaveBo, vipRights);

        iVipRightsService.save(vipRights);
    }

    @Override
    public IPage<VipRightsVo> pageInfo(VipRightsPageBo vipRightsPageBo) {
        Page<VipRights> initPage = PageUtil.initPage(vipRightsPageBo.getPage(), vipRightsPageBo.getLimit());

        QueryWrapper<VipRights> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(vipRightsPageBo.getName()), VipRights::getName, vipRightsPageBo.getName())
                .eq(StringUtils.isNotBlank(vipRightsPageBo.getRightsCode()), VipRights::getRightsCode, vipRightsPageBo.getRightsCode())
                .orderByAsc(VipRights::getSort)
                .orderByDesc(VipRights::getCreateTime);

        IPage<VipRights> iPage = iVipRightsService.page(initPage, queryWrapper);
        List<VipRightsVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (VipRights record : iPage.getRecords()) {
                VipRightsVo vo = new VipRightsVo();
                BeanUtils.copyProperties(record, vo);

                voList.add(vo);
            }
        }

        IPage<VipRightsVo> voIPage = PageUtil.copyPage(iPage, voList);
        return voIPage;
    }

    @Override
    public VipRightsVo info(VipRightsInfoBo vipRightsInfoBo) {
        VipRights vipRights = iVipRightsService.getById(vipRightsInfoBo.getId());
        if (Objects.isNull(vipRights)) {
            throw new BizException("信息不存在");
        }

        VipRightsVo vo = new VipRightsVo();
        BeanUtils.copyProperties(vipRights, vo);
        return vo;
    }

    @Override
    public void updateInfo(VipRightsUpdateBo vipRightsUpdateBo) {
        VipRights vipRights = new VipRights();
        BeanUtils.copyProperties(vipRightsUpdateBo, vipRights);

        iVipRightsService.updateById(vipRights);
    }

    @Override
    public void delete(VipRightsInfoBo vipRightsInfoBo) {
        iVipRightsService.removeById(vipRightsInfoBo.getId());
    }

    @Override
    public List<VipRightsVo> list() {
        QueryWrapper<VipRights> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(VipRights::getSort);
        List<VipRights> vipRightsList = iVipRightsService.list(queryWrapper);

        List<VipRightsVo> voList = CollectionUtils.isNotEmpty(vipRightsList) ? vipRightsList.stream().map(vipRights -> {
            VipRightsVo vo = new VipRightsVo();
            BeanUtils.copyProperties(vipRights, vo);
            return vo;
        }).collect(Collectors.toList()) : Collections.emptyList();

        return voList;
    }

    @Override
    public List<AppVipRightsCompareItemVO> listCompareItems() {
        QueryWrapper<VipRights> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .isNotNull(VipRights::getRightsCode)
                .ne(VipRights::getRightsCode, "")
                .orderByAsc(VipRights::getSort);
        List<VipRights> compareRows = iVipRightsService.list(queryWrapper);
        if (CollectionUtils.isEmpty(compareRows)) {
            return buildDefaultCompareItems();
        }
        return compareRows.stream()
                .map(this::toCompareItem)
                .collect(Collectors.toList());
    }

    private AppVipRightsCompareItemVO toCompareItem(VipRights rights) {
        AppVipRightsCompareItemVO vo = new AppVipRightsCompareItemVO();
        vo.setCode(rights.getRightsCode());
        vo.setLabel(rights.getName());
        vo.setFree(resolveFreeText(rights));
        vo.setVip(rights.getVipText());
        return vo;
    }

    private String resolveFreeText(VipRights rights) {
        if (RIGHTS_CODE_AI.equals(rights.getRightsCode())) {
            return aiConfigReader.getDailyLimit() + "次/日";
        }
        if (RIGHTS_CODE_OCR.equals(rights.getRightsCode())) {
            return ocrConfigReader.getDailyLimit() + "次/日";
        }
        return rights.getFreeText();
    }

    private List<AppVipRightsCompareItemVO> buildDefaultCompareItems() {
        List<AppVipRightsCompareItemVO> defaults = new ArrayList<>();
        defaults.add(buildDefaultItem("ai", "AI 答疑", aiConfigReader.getDailyLimit() + "次/日", "无限次"));
        defaults.add(buildDefaultItem("ocr", "OCR 拍照搜题", ocrConfigReader.getDailyLimit() + "次/日", "无限次"));
        defaults.add(buildDefaultItem("video", "高清视频解析", "·", "✓"));
        defaults.add(buildDefaultItem("report", "专属学情报告", "基础版", "详细版"));
        defaults.add(buildDefaultItem("ad_free", "广告过滤", "·", "✓"));
        return defaults;
    }

    private AppVipRightsCompareItemVO buildDefaultItem(String code, String label, String free, String vip) {
        AppVipRightsCompareItemVO item = new AppVipRightsCompareItemVO();
        item.setCode(code);
        item.setLabel(label);
        item.setFree(free);
        item.setVip(vip);
        return item;
    }
}
