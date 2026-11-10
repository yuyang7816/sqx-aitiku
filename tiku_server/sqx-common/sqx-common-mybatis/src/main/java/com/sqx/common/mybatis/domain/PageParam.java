package com.sqx.common.mybatis.domain;

import lombok.Data;

/**
 * @author chengming
 * @since 2024/9/11
 */
@Data
public class PageParam {

    /**
     * 当前页
     */
    private Long page;

    /**
     * 页大小
     */
    private Long limit;
}
