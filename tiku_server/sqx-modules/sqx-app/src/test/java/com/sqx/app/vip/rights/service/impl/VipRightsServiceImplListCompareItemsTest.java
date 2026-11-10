package com.sqx.app.vip.rights.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.base.ai.support.AiConfigReader;
import com.sqx.app.base.ocr.support.OcrConfigReader;
import com.sqx.app.vip.rights.db.service.IVipRightsService;
import com.sqx.app.vip.rights.domain.entity.VipRights;
import com.sqx.app.vip.rights.domain.vo.AppVipRightsCompareItemVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/vipRights 权益对比表单元测试
 */
@ExtendWith(MockitoExtension.class)
class VipRightsServiceImplListCompareItemsTest {

    @Mock
    private IVipRightsService iVipRightsService;

    @Mock
    private AiConfigReader aiConfigReader;

    @Mock
    private OcrConfigReader ocrConfigReader;

    @InjectMocks
    private VipRightsServiceImpl vipRightsService;

    @Test
    void listCompareItems_whenDbEmpty_shouldReturnFiveDefaultItems() {
        when(iVipRightsService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());
        when(aiConfigReader.getDailyLimit()).thenReturn(5);
        when(ocrConfigReader.getDailyLimit()).thenReturn(5);

        List<AppVipRightsCompareItemVO> result = vipRightsService.listCompareItems();

        assertEquals(5, result.size());
        assertEquals("ai", result.get(0).getCode());
        assertEquals("AI 答疑", result.get(0).getLabel());
        assertEquals("5次/日", result.get(0).getFree());
        assertEquals("无限次", result.get(0).getVip());
        assertEquals("ocr", result.get(1).getCode());
        assertEquals("video", result.get(2).getCode());
        assertEquals("report", result.get(3).getCode());
        assertEquals("ad_free", result.get(4).getCode());
        assertEquals("详细版", result.get(3).getVip());
    }

    @Test
    void listCompareItems_shouldMapDbRowsAndSyncAiOcrDailyLimit() {
        when(aiConfigReader.getDailyLimit()).thenReturn(3);
        when(ocrConfigReader.getDailyLimit()).thenReturn(7);
        when(iVipRightsService.list(any(QueryWrapper.class))).thenReturn(Arrays.asList(
                buildRow("ai", "AI 答疑", "5次/日", "无限次", 1),
                buildRow("ocr", "OCR 拍照搜题", "5次/日", "无限次", 2),
                buildRow("video", "高清视频解析", "·", "✓", 3)
        ));

        List<AppVipRightsCompareItemVO> result = vipRightsService.listCompareItems();

        assertEquals(3, result.size());
        assertEquals("3次/日", result.get(0).getFree());
        assertEquals("7次/日", result.get(1).getFree());
        assertEquals("·", result.get(2).getFree());
        assertEquals("✓", result.get(2).getVip());

        ArgumentCaptor<QueryWrapper> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(iVipRightsService).list(captor.capture());
        assertNotNull(captor.getValue());
    }

    @Test
    void listCompareItems_shouldIncludeAllRequiredRightsCodes() {
        when(iVipRightsService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());
        when(aiConfigReader.getDailyLimit()).thenReturn(5);
        when(ocrConfigReader.getDailyLimit()).thenReturn(5);

        List<AppVipRightsCompareItemVO> result = vipRightsService.listCompareItems();

        List<String> codes = Arrays.asList(
                result.get(0).getCode(),
                result.get(1).getCode(),
                result.get(2).getCode(),
                result.get(3).getCode(),
                result.get(4).getCode()
        );
        assertTrue(codes.containsAll(Arrays.asList("ai", "ocr", "video", "report", "ad_free")));
    }

    private VipRights buildRow(String code, String name, String freeText, String vipText, int sort) {
        VipRights rights = new VipRights();
        rights.setRightsCode(code);
        rights.setName(name);
        rights.setFreeText(freeText);
        rights.setVipText(vipText);
        rights.setSort(sort);
        return rights;
    }
}
