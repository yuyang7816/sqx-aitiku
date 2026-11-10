package com.sqx.common.mybatis.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chengming
 * @since 2024/7/15
 */
public class PageUtil {

    public static <T> Page<T> initPage(Long page, Long limit) {
        if (Objects.isNull(page) || 0 == page) {
            page = 1L;
        }

        if (Objects.isNull(limit) || 0 == limit) {
            limit = 10L;
        }

        return new Page<>(page, limit);
    }

    public static <E,T> IPage<T> copyPage(IPage<E> iPage, List<T> voList) {
        IPage<T> page = new Page<>();
        page.setPages(iPage.getPages());
        page.setSize(iPage.getSize());
        page.setCurrent(iPage.getCurrent());
        page.setTotal(iPage.getTotal());
        page.setRecords(voList);

        return page;
    }

    public static <T> IPage<T> manualPage(Page<T> page, List<T> list) {
        IPage<T> iPage = new Page<>();

        List<T> pageList = list.stream().skip((page.getCurrent() - 1) * page.getSize())
                .limit(page.getSize())
                .collect(Collectors.toList());

        long pages = list.size() % page.getSize() == 0 ? list.size() / page.getSize() : (list.size() / page.getSize()) + 1;

        iPage.setCurrent(page.getCurrent());
        iPage.setSize(page.getSize());
        iPage.setTotal(list.size());
        iPage.setPages(pages);
        iPage.setRecords(pageList);

        return iPage;
    }
}
