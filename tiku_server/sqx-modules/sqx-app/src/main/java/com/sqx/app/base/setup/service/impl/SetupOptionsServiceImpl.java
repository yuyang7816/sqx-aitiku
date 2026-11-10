package com.sqx.app.base.setup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.common.core.utils.TextGarbledUtils;
import com.sqx.app.base.setup.constants.SetupDictConstant;
import com.sqx.app.base.setup.domain.vo.SetupGradeOptionVO;
import com.sqx.app.base.setup.domain.vo.SetupGradeStageOptionVO;
import com.sqx.app.base.setup.domain.vo.SetupOptionsVO;
import com.sqx.app.base.setup.domain.vo.SetupSubjectOptionVO;
import com.sqx.app.base.setup.domain.vo.SetupVersionOptionVO;
import com.sqx.app.base.setup.service.SetupOptionsService;
import com.sqx.app.base.setup.support.SetupOptionsDefaults;
import com.sqx.app.base.setup.support.SetupQueryResolveSupport;
import com.sqx.app.base.setup.support.SetupSubjectStageDefaults;
import com.sqx.system.db.entity.SysDictEntity;
import com.sqx.system.db.service.ISysDictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学习计划选项：从 sys_dict 批量读取 app_setup_* 字典并映射为 plan.vue 兼容结构
 */
@Service
public class SetupOptionsServiceImpl implements SetupOptionsService {

    private enum DictBucket {
        GRADE_STAGE(SetupDictConstant.TYPE_GRADE_STAGE),
        GRADE(SetupDictConstant.TYPE_GRADE),
        SUBJECT(SetupDictConstant.TYPE_SUBJECT),
        VERSION(SetupDictConstant.TYPE_VERSION);

        private final String type;

        DictBucket(String type) {
            this.type = type;
        }
    }

    @Autowired
    private ISysDictService sysDictService;

    @Override
    public SetupOptionsVO getSetupOptions() {
        List<SysDictEntity> dictItems = listSetupDictItems();
        Map<DictBucket, List<SysDictEntity>> grouped = groupByBucket(dictItems);

        List<SetupGradeStageOptionVO> gradeStages = mapGradeStages(grouped.get(DictBucket.GRADE_STAGE));
        List<SetupGradeOptionVO> grades = mapGrades(grouped.get(DictBucket.GRADE), gradeStages);

        SetupOptionsVO vo = new SetupOptionsVO();
        vo.setGradeStages(gradeStages);
        vo.setGrades(grades);
        vo.setSubjects(mapSubjects(grouped.get(DictBucket.SUBJECT)));
        vo.setVersions(mapVersions(grouped.get(DictBucket.VERSION)));
        return vo;
    }

    private List<SysDictEntity> listSetupDictItems() {
        return sysDictService.list(new LambdaQueryWrapper<SysDictEntity>()
                .in(SysDictEntity::getType,
                        SetupDictConstant.TYPE_GRADE_STAGE,
                        SetupDictConstant.TYPE_GRADE,
                        SetupDictConstant.TYPE_SUBJECT,
                        SetupDictConstant.TYPE_VERSION)
                .and(wrapper -> wrapper.eq(SysDictEntity::getStatus, 1).or().isNull(SysDictEntity::getStatus))
                .orderByAsc(SysDictEntity::getOrderNum)
                .orderByAsc(SysDictEntity::getId))
                .stream()
                .filter(this::isValidDictItem)
                .collect(Collectors.toList());
    }

    private boolean isValidDictItem(SysDictEntity dict) {
        if (dict == null) {
            return false;
        }
        return !TextGarbledUtils.hasGarbledText(dict.getName(), dict.getCode(), dict.getValue());
    }

    private Map<DictBucket, List<SysDictEntity>> groupByBucket(List<SysDictEntity> dictItems) {
        Map<String, List<SysDictEntity>> byType = dictItems.stream()
                .collect(Collectors.groupingBy(SysDictEntity::getType));

        Map<DictBucket, List<SysDictEntity>> grouped = new EnumMap<>(DictBucket.class);
        for (DictBucket bucket : DictBucket.values()) {
            grouped.put(bucket, sortDictItems(byType.getOrDefault(bucket.type, new ArrayList<>())));
        }
        return grouped;
    }

    private List<SysDictEntity> sortDictItems(List<SysDictEntity> items) {
        return items.stream()
                .sorted(Comparator
                        .comparing(SysDictEntity::getOrderNum, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysDictEntity::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private List<SetupGradeStageOptionVO> mapGradeStages(List<SysDictEntity> items) {
        if (items == null || items.isEmpty()) {
            return SetupOptionsDefaults.defaultGradeStages();
        }
        return items.stream().map(this::toGradeStageOption).collect(Collectors.toList());
    }

    private List<SetupGradeOptionVO> mapGrades(List<SysDictEntity> items, List<SetupGradeStageOptionVO> gradeStages) {
        Map<String, SetupGradeStageOptionVO> stageByCode = buildStageLookup(gradeStages);
        if (items == null || items.isEmpty()) {
            return SetupOptionsDefaults.defaultGrades();
        }
        return items.stream()
                .map(item -> toGradeOption(item, stageByCode))
                .collect(Collectors.toList());
    }

    private Map<String, SetupGradeStageOptionVO> buildStageLookup(List<SetupGradeStageOptionVO> gradeStages) {
        Map<String, SetupGradeStageOptionVO> lookup = new HashMap<>();
        if (gradeStages == null) {
            return lookup;
        }
        for (SetupGradeStageOptionVO stage : gradeStages) {
            if (stage != null && StringUtils.isNotBlank(stage.getValue())) {
                lookup.put(stage.getValue().trim(), stage);
            }
        }
        return lookup;
    }

    private List<SetupSubjectOptionVO> mapSubjects(List<SysDictEntity> items) {
        if (items == null || items.isEmpty()) {
            return SetupOptionsDefaults.defaultSubjects();
        }
        return items.stream().map(this::toSubjectOption).collect(Collectors.toList());
    }

    private List<SetupVersionOptionVO> mapVersions(List<SysDictEntity> items) {
        if (items == null || items.isEmpty()) {
            return SetupOptionsDefaults.defaultVersions();
        }
        return items.stream().map(this::toVersionOption).collect(Collectors.toList());
    }

    private SetupGradeStageOptionVO toGradeStageOption(SysDictEntity dict) {
        SetupGradeStageOptionVO vo = new SetupGradeStageOptionVO();
        vo.setLabel(StringUtils.defaultIfBlank(dict.getName(), dict.getValue()));
        vo.setValue(StringUtils.defaultIfBlank(dict.getCode(), dict.getValue()));
        return vo;
    }

    private SetupGradeOptionVO toGradeOption(SysDictEntity dict, Map<String, SetupGradeStageOptionVO> stageByCode) {
        SetupGradeOptionVO vo = new SetupGradeOptionVO();
        String apiCode = StringUtils.defaultIfBlank(dict.getCode(), dict.getName());
        String shortLabel = SetupQueryResolveSupport.resolveGradeShort(apiCode);
        vo.setLabel(StringUtils.defaultIfBlank(shortLabel, apiCode));
        vo.setValue(StringUtils.defaultIfBlank(dict.getValue(), dict.getName()));

        String stageCode = resolveStageCode(dict);
        vo.setStageCode(stageCode);
        SetupGradeStageOptionVO stage = stageByCode.get(stageCode);
        if (stage != null) {
            vo.setStageName(stage.getLabel());
        } else if (StringUtils.isNotBlank(stageCode)) {
            vo.setStageName(stageCode);
        } else {
            vo.setStageName(inferStageNameFromGradeValue(vo.getValue()));
            vo.setStageCode(inferStageCodeFromGradeValue(vo.getValue()));
        }
        return vo;
    }

    /**
     * 年级字典 remark 存学段编码（primary/junior/senior），与 admin 年级学科字典页约定一致
     */
    private String resolveStageCode(SysDictEntity dict) {
        return StringUtils.trimToNull(dict.getRemark());
    }

    private String inferStageCodeFromGradeValue(String gradeValue) {
        if (StringUtils.isBlank(gradeValue)) {
            return "junior";
        }
        if (gradeValue.startsWith("小学")) {
            return "primary";
        }
        if (gradeValue.startsWith("高中")) {
            return "senior";
        }
        return "junior";
    }

    private String inferStageNameFromGradeValue(String gradeValue) {
        String stageCode = inferStageCodeFromGradeValue(gradeValue);
        if ("primary".equals(stageCode)) {
            return "小学";
        }
        if ("senior".equals(stageCode)) {
            return "高中";
        }
        return "初中";
    }

    private SetupSubjectOptionVO toSubjectOption(SysDictEntity dict) {
        SetupSubjectOptionVO vo = new SetupSubjectOptionVO();
        String name = StringUtils.defaultIfBlank(dict.getName(), dict.getValue());
        vo.setName(name);
        vo.setIcon(SetupSubjectStageDefaults.resolveSubjectIcon(name, dict.getRemark()));
        List<String> stageCodes = SetupSubjectStageDefaults.parseStageCodes(dict.getRemark());
        if (stageCodes.isEmpty()) {
            stageCodes = SetupSubjectStageDefaults.defaultStagesForSubject(name);
        }
        vo.setStageCodes(stageCodes);
        vo.setActive(Boolean.FALSE);
        return vo;
    }

    private SetupVersionOptionVO toVersionOption(SysDictEntity dict) {
        SetupVersionOptionVO vo = new SetupVersionOptionVO();
        vo.setLabel(StringUtils.defaultIfBlank(dict.getName(), dict.getValue()));
        vo.setValue(StringUtils.defaultIfBlank(dict.getValue(), dict.getName()));
        return vo;
    }
}
