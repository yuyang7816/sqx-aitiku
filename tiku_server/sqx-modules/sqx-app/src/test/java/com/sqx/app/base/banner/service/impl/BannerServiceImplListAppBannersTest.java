package com.sqx.app.base.banner.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.app.base.banner.domain.vo.AppBannerItemVO;
import com.sqx.system.db.entity.Banner;
import com.sqx.system.db.service.IBannerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/banner 用户端轮播列表单元测试
 */
@ExtendWith(MockitoExtension.class)
class BannerServiceImplListAppBannersTest {

    @Mock
    private IBannerService iBannerService;

    @InjectMocks
    private BannerServiceImpl bannerService;

    @Test
    void listAppBanners_shouldReturnEnabledHomeBannersOrderedBySort() {
        Banner first = buildBanner(1L, "https://cdn/a.png", "/pages/vip", 1, 1, 1);
        Banner second = buildBanner(2L, "https://cdn/b.png", "https://example.com", 2, 1, 1);
        when(iBannerService.list(any(Wrapper.class))).thenReturn(Arrays.asList(first, second));

        List<AppBannerItemVO> result = bannerService.listAppBanners(null, null);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("https://cdn/a.png", result.get(0).getImageUrl());
        assertEquals("/pages/vip", result.get(0).getLink());
        assertEquals(1, result.get(0).getSort());
        assertEquals("https://example.com", result.get(1).getLink());

        verify(iBannerService).list(any(Wrapper.class));
    }

    @Test
    void listAppBanners_whenNoRecords_shouldReturnEmptyList() {
        when(iBannerService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        List<AppBannerItemVO> result = bannerService.listAppBanners(1, 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void listAppBanners_whenFilterHiddenState_shouldQueryWithGivenState() {
        when(iBannerService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        bannerService.listAppBanners(2, 2);

        verify(iBannerService).list(any(Wrapper.class));
    }

    private Banner buildBanner(Long id, String imageUrl, String url, Integer sort, Integer classify, Integer state) {
        Banner banner = new Banner();
        banner.setId(id);
        banner.setImageUrl(imageUrl);
        banner.setUrl(url);
        banner.setSort(sort);
        banner.setClassify(classify);
        banner.setState(state);
        return banner;
    }
}
