package com.sqx.app.base.setup.support;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学科与学段默认映射：remark 未配置学段时按学科名兜底
 */
public final class SetupSubjectStageDefaults {

    private static final Set<String> STAGE_CODES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("primary", "junior", "senior")));

    private static final Set<String> KNOWN_ICONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "file-text", "edit-pen", "email", "reload", "more-circle", "grid",
            "bookmark", "star", "photo", "calendar", "map", "tags", "clock"
    )));

    private static final Map<String, List<String>> DEFAULT_STAGES_BY_SUBJECT;

    static {
        Map<String, List<String>> map = new HashMap<>();
        map.put("数学", Arrays.asList("primary", "junior", "senior"));
        map.put("语文", Arrays.asList("primary", "junior", "senior"));
        map.put("英语", Arrays.asList("primary", "junior", "senior"));
        map.put("物理", Arrays.asList("junior", "senior"));
        map.put("化学", Arrays.asList("junior", "senior"));
        map.put("生物", Arrays.asList("junior", "senior"));
        map.put("历史", Arrays.asList("junior", "senior"));
        map.put("地理", Arrays.asList("junior", "senior"));
        map.put("政治", Arrays.asList("junior", "senior"));
        DEFAULT_STAGES_BY_SUBJECT = Collections.unmodifiableMap(map);
    }

    private SetupSubjectStageDefaults() {
    }

    public static boolean isKnownIcon(String remark) {
        return StringUtils.isNotBlank(remark) && KNOWN_ICONS.contains(StringUtils.trim(remark));
    }

    public static List<String> defaultStagesForSubject(String subjectName) {
        String name = StringUtils.trimToEmpty(subjectName);
        if (StringUtils.isBlank(name)) {
            return Arrays.asList("junior", "senior");
        }
        return DEFAULT_STAGES_BY_SUBJECT.getOrDefault(name, Arrays.asList("junior", "senior"));
    }

    public static List<String> parseStageCodes(String remark) {
        if (StringUtils.isBlank(remark) || isKnownIcon(remark)) {
            return Collections.emptyList();
        }
        List<String> parsed = Arrays.stream(StringUtils.split(remark, ','))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .filter(STAGE_CODES::contains)
                .distinct()
                .collect(Collectors.toList());
        return parsed.isEmpty() ? Collections.emptyList() : parsed;
    }

    public static String resolveSubjectIcon(String subjectName, String remark) {
        if (isKnownIcon(remark)) {
            return StringUtils.trim(remark);
        }
        String name = StringUtils.trimToEmpty(subjectName);
        switch (name) {
            case "数学":
                return "file-text";
            case "语文":
                return "edit-pen";
            case "英语":
                return "email";
            case "物理":
                return "reload";
            case "化学":
                return "more-circle";
            case "生物":
                return "grid";
            case "历史":
                return "clock";
            case "地理":
                return "map";
            case "政治":
                return "star";
            default:
                return "file-text";
        }
    }
}
