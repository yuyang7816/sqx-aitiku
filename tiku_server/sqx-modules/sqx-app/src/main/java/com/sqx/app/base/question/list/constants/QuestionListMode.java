package com.sqx.app.base.question.list.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 练习题目拉取模式（与 uniapp practiceSource.mode 对齐）
 */
public interface QuestionListMode {

    /** 全部题目 */
    String ALL = "all";

    /** 仅错题（用户答错记录） */
    String WRONG = "wrong";

    /** 指定题目（需配合 questionIds） */
    String SINGLE = "single";

    Set<String> VALID_MODES = Collections.unmodifiableSet(
            Arrays.asList(ALL, WRONG, SINGLE).stream().collect(Collectors.toSet())
    );
}
