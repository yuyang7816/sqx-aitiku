package com.sqx.app.base.setup.support;

import com.sqx.app.base.setup.domain.vo.SetupGradeOptionVO;
import com.sqx.app.base.setup.domain.vo.SetupGradeStageOptionVO;
import com.sqx.app.base.setup.domain.vo.SetupSubjectOptionVO;
import com.sqx.app.base.setup.domain.vo.SetupVersionOptionVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 学习计划默认选项（与 uniapp common/mock/setup.js 对齐，字典为空时兜底）
 */
public final class SetupOptionsDefaults {

    private SetupOptionsDefaults() {
    }

    public static List<SetupGradeStageOptionVO> defaultGradeStages() {
        return Arrays.asList(
                stage("小学", "primary"),
                stage("初中", "junior"),
                stage("高中", "senior")
        );
    }

    public static List<SetupGradeOptionVO> defaultGrades() {
        List<SetupGradeOptionVO> list = new ArrayList<>();
        list.add(grade("一年级", "小学一年级", "primary", "小学"));
        list.add(grade("二年级", "小学二年级", "primary", "小学"));
        list.add(grade("三年级", "小学三年级", "primary", "小学"));
        list.add(grade("四年级", "小学四年级", "primary", "小学"));
        list.add(grade("五年级", "小学五年级", "primary", "小学"));
        list.add(grade("六年级", "小学六年级", "primary", "小学"));
        list.add(grade("初一", "初中一年级", "junior", "初中"));
        list.add(grade("初二", "初中二年级", "junior", "初中"));
        list.add(grade("初三", "初中三年级", "junior", "初中"));
        list.add(grade("高一", "高中一年级", "senior", "高中"));
        list.add(grade("高二", "高中二年级", "senior", "高中"));
        list.add(grade("高三", "高中三年级", "senior", "高中"));
        return list;
    }

    public static List<SetupSubjectOptionVO> defaultSubjects() {
        return Arrays.asList(
                subject("数学", "file-text", "primary", "junior", "senior"),
                subject("语文", "edit-pen", "primary", "junior", "senior"),
                subject("英语", "email", "primary", "junior", "senior"),
                subject("物理", "reload", "junior", "senior"),
                subject("化学", "more-circle", "junior", "senior"),
                subject("生物", "grid", "junior", "senior")
        );
    }

    public static List<SetupVersionOptionVO> defaultVersions() {
        return Arrays.asList(
                version("人教版", "人教版"),
                version("北师大版", "北师大版"),
                version("苏教版", "苏教版")
        );
    }

    private static SetupGradeStageOptionVO stage(String label, String value) {
        SetupGradeStageOptionVO vo = new SetupGradeStageOptionVO();
        vo.setLabel(label);
        vo.setValue(value);
        return vo;
    }

    private static SetupGradeOptionVO grade(String label, String value, String stageCode, String stageName) {
        SetupGradeOptionVO vo = new SetupGradeOptionVO();
        vo.setLabel(label);
        vo.setValue(value);
        vo.setStageCode(stageCode);
        vo.setStageName(stageName);
        return vo;
    }

    private static SetupSubjectOptionVO subject(String name, String icon, String... stageCodes) {
        SetupSubjectOptionVO vo = new SetupSubjectOptionVO();
        vo.setName(name);
        vo.setIcon(icon);
        vo.setStageCodes(Arrays.asList(stageCodes));
        vo.setActive(Boolean.FALSE);
        return vo;
    }

    private static SetupVersionOptionVO version(String label, String value) {
        SetupVersionOptionVO vo = new SetupVersionOptionVO();
        vo.setLabel(label);
        vo.setValue(value);
        return vo;
    }
}
