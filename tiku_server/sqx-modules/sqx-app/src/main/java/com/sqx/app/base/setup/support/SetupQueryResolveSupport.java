package com.sqx.app.base.setup.support;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户端 query 中的 grade/subject/version 业务编码 ↔ 库表展示字段互转。
 * 约定：接口入参仅传 ASCII 编码（如 junior_1、math、renjiao），禁止传中文。
 */
public final class SetupQueryResolveSupport {

    private static final Map<String, String> GRADE_CODE_TO_VALUE;
    private static final Map<String, String> GRADE_CODE_TO_SHORT;
    private static final Map<String, String> SUBJECT_CODE_TO_VALUE;
    private static final Map<String, String> VERSION_CODE_TO_VALUE;

    static {
        Map<String, String> gradeValue = new HashMap<>();
        Map<String, String> gradeShort = new HashMap<>();
        gradeValue.put("primary_1", "小学一年级");
        gradeShort.put("primary_1", "小一");
        gradeValue.put("primary_2", "小学二年级");
        gradeShort.put("primary_2", "小二");
        gradeValue.put("primary_3", "小学三年级");
        gradeShort.put("primary_3", "小三");
        gradeValue.put("primary_4", "小学四年级");
        gradeShort.put("primary_4", "小四");
        gradeValue.put("primary_5", "小学五年级");
        gradeShort.put("primary_5", "小五");
        gradeValue.put("primary_6", "小学六年级");
        gradeShort.put("primary_6", "小六");
        gradeValue.put("junior_1", "初中一年级");
        gradeShort.put("junior_1", "初一");
        gradeValue.put("junior_2", "初中二年级");
        gradeShort.put("junior_2", "初二");
        gradeValue.put("junior_3", "初中三年级");
        gradeShort.put("junior_3", "初三");
        gradeValue.put("senior_1", "高中一年级");
        gradeShort.put("senior_1", "高一");
        gradeValue.put("senior_2", "高中二年级");
        gradeShort.put("senior_2", "高二");
        gradeValue.put("senior_3", "高中三年级");
        gradeShort.put("senior_3", "高三");
        GRADE_CODE_TO_VALUE = Collections.unmodifiableMap(gradeValue);
        GRADE_CODE_TO_SHORT = Collections.unmodifiableMap(gradeShort);

        Map<String, String> subjectValue = new HashMap<>();
        subjectValue.put("math", "数学");
        subjectValue.put("chinese", "语文");
        subjectValue.put("english", "英语");
        subjectValue.put("physics", "物理");
        subjectValue.put("chemistry", "化学");
        subjectValue.put("biology", "生物");
        subjectValue.put("history", "历史");
        subjectValue.put("geography", "地理");
        subjectValue.put("politics", "政治");
        SUBJECT_CODE_TO_VALUE = Collections.unmodifiableMap(subjectValue);

        Map<String, String> versionValue = new HashMap<>();
        versionValue.put("renjiao", "人教版");
        versionValue.put("beishida", "北师大版");
        versionValue.put("sujiao", "苏教版");
        versionValue.put("hujiao", "沪教版");
        VERSION_CODE_TO_VALUE = Collections.unmodifiableMap(versionValue);
    }

    private SetupQueryResolveSupport() {
    }

    public static String resolveGradeValue(String raw) {
        String token = normalizeToken(raw);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        if (GRADE_CODE_TO_VALUE.containsKey(token)) {
            return GRADE_CODE_TO_VALUE.get(token);
        }
        if (containsHan(token)) {
            return token;
        }
        return null;
    }

    public static String resolveGradeShort(String raw) {
        String token = normalizeToken(raw);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        if (GRADE_CODE_TO_SHORT.containsKey(token)) {
            return GRADE_CODE_TO_SHORT.get(token);
        }
        String gradeValue = resolveGradeValue(token);
        if (StringUtils.isNotBlank(gradeValue) && gradeValue.startsWith("初中")) {
            if (gradeValue.contains("一")) {
                return "初一";
            }
            if (gradeValue.contains("二")) {
                return "初二";
            }
            if (gradeValue.contains("三")) {
                return "初三";
            }
        }
        return null;
    }

    public static String resolveSubjectValue(String raw) {
        String token = normalizeToken(raw);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        if (SUBJECT_CODE_TO_VALUE.containsKey(token)) {
            return SUBJECT_CODE_TO_VALUE.get(token);
        }
        if (containsHan(token)) {
            return token;
        }
        return null;
    }

    public static String resolveVersionValue(String raw) {
        String token = normalizeToken(raw);
        if (StringUtils.isBlank(token)) {
            return null;
        }
        if (VERSION_CODE_TO_VALUE.containsKey(token)) {
            return VERSION_CODE_TO_VALUE.get(token);
        }
        if (containsHan(token)) {
            return normalizeVersionLabel(token);
        }
        return null;
    }

    private static String normalizeToken(String raw) {
        return StringUtils.trimToNull(raw);
    }

    private static String normalizeVersionLabel(String version) {
        return version.replace("（默认）", "").trim();
    }

    private static boolean containsHan(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (Character.UnicodeScript.of(text.charAt(i)) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }
}
