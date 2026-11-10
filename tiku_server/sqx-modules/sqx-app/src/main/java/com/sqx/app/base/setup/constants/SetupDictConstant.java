package com.sqx.app.base.setup.constants;

/**
 * 学习计划字典类型（与 admin setup-dict 及 sys_dict.type 一致）
 */
public final class SetupDictConstant {

    private SetupDictConstant() {
    }

    /** 学段字典 type */
    public static final String TYPE_GRADE_STAGE = "app_setup_grade_stage";

    /** 年级字典 type */
    public static final String TYPE_GRADE = "app_setup_grade";

    /** 学科字典 type */
    public static final String TYPE_SUBJECT = "app_setup_subject";

    /** 教材版本字典 type */
    public static final String TYPE_VERSION = "app_setup_version";
}
