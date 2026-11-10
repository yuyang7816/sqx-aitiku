package com.sqx.app.base.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.search.constants.SearchRecordConstant;
import com.sqx.app.base.search.domain.bo.AppSearchRecordUpsertBO;
import com.sqx.app.base.search.domain.vo.AppSearchRecordItemVO;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.UserSearchRecord;
import com.sqx.system.db.service.IUserSearchRecordService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET/POST/DELETE /app/search/records 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SearchRecordServiceImplTest {

    @Mock
    private IUserSearchRecordService iUserSearchRecordService;

    @InjectMocks
    private SearchRecordServiceImpl searchRecordService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, UserSearchRecord.class);
    }

    @Test
    void listRecords_shouldReturnFormattedItems() {
        UserSearchRecord record = buildRecord(1L, 100L, "一次函数图像性质", SearchRecordConstant.SOURCE_MANUAL, null);
        record.setUpdateTime(LocalDateTime.now());

        when(iUserSearchRecordService.list(any(Wrapper.class))).thenReturn(Collections.singletonList(record));

        List<AppSearchRecordItemVO> result = searchRecordService.listRecords(100L);

        assertEquals(1, result.size());
        assertEquals("一次函数图像性质", result.get(0).getTitle());
        assertEquals(SearchRecordConstant.SOURCE_MANUAL, result.get(0).getSource());
        assertEquals("刚刚", result.get(0).getTime());
    }

    @Test
    void upsertRecord_shouldInsertNewRecord() {
        AppSearchRecordUpsertBO upsert = new AppSearchRecordUpsertBO();
        upsert.setTitle("  已知二次函数 y=ax²  ");
        upsert.setSource(SearchRecordConstant.SOURCE_PHOTO);
        upsert.setImageUrl(" /upload/ocr/a.jpg ");

        when(iUserSearchRecordService.getOne(any(Wrapper.class))).thenReturn(null);
        when(iUserSearchRecordService.count(any(Wrapper.class))).thenReturn(1L);

        AppSearchRecordItemVO result = searchRecordService.upsertRecord(100L, upsert);

        assertNotNull(result);
        assertEquals("已知二次函数 y=ax²", result.getTitle());
        assertEquals(SearchRecordConstant.SOURCE_PHOTO, result.getSource());
        assertEquals("/upload/ocr/a.jpg", result.getImageUrl());

        ArgumentCaptor<UserSearchRecord> captor = ArgumentCaptor.forClass(UserSearchRecord.class);
        verify(iUserSearchRecordService).save(captor.capture());
        assertEquals(100L, captor.getValue().getUserId());
    }

    @Test
    void upsertRecord_shouldUpdateExistingRecordByTitle() {
        UserSearchRecord existing = buildRecord(1L, 100L, "英语完形填空", SearchRecordConstant.SOURCE_MANUAL, null);
        AppSearchRecordUpsertBO upsert = new AppSearchRecordUpsertBO();
        upsert.setTitle("英语完形填空");
        upsert.setSource(SearchRecordConstant.SOURCE_AI);
        upsert.setImageUrl("");

        when(iUserSearchRecordService.getOne(any(Wrapper.class))).thenReturn(existing);
        when(iUserSearchRecordService.count(any(Wrapper.class))).thenReturn(1L);

        AppSearchRecordItemVO result = searchRecordService.upsertRecord(100L, upsert);

        assertEquals(SearchRecordConstant.SOURCE_AI, result.getSource());
        verify(iUserSearchRecordService).updateById(existing);
        verify(iUserSearchRecordService, never()).save(any());
    }

    @Test
    void upsertRecord_shouldRejectBlankTitle() {
        AppSearchRecordUpsertBO upsert = new AppSearchRecordUpsertBO();
        upsert.setTitle("   ");
        upsert.setSource(SearchRecordConstant.SOURCE_MANUAL);

        assertThrows(BizException.class, () -> searchRecordService.upsertRecord(100L, upsert));
    }

    @Test
    void upsertRecord_shouldTrimOverflowRecords() {
        AppSearchRecordUpsertBO upsert = new AppSearchRecordUpsertBO();
        upsert.setTitle("新题目");
        upsert.setSource(SearchRecordConstant.SOURCE_MANUAL);

        when(iUserSearchRecordService.getOne(any(Wrapper.class))).thenReturn(null);
        when(iUserSearchRecordService.count(any(Wrapper.class))).thenReturn(21L);

        List<UserSearchRecord> oldest = new ArrayList<>();
        oldest.add(buildRecord(99L, 100L, "旧题", SearchRecordConstant.SOURCE_MANUAL, null));
        when(iUserSearchRecordService.list(any(Wrapper.class))).thenReturn(oldest);

        searchRecordService.upsertRecord(100L, upsert);

        verify(iUserSearchRecordService).removeByIds(Collections.singletonList(99L));
    }

    @Test
    void clearRecords_shouldRemoveAllForUser() {
        searchRecordService.clearRecords(100L);
        verify(iUserSearchRecordService).remove(any(Wrapper.class));
    }

    @Test
    void normalizeSource_shouldFallbackToPhotoWhenInvalid() {
        assertEquals(SearchRecordConstant.SOURCE_PHOTO,
                searchRecordService.normalizeSource("未知来源"));
    }

    private UserSearchRecord buildRecord(Long id, Long userId, String title, String source, String imageUrl) {
        UserSearchRecord record = new UserSearchRecord();
        record.setId(id);
        record.setUserId(userId);
        record.setTitle(title);
        record.setSource(source);
        record.setImageUrl(imageUrl);
        record.setCreateTime(LocalDateTime.now().minusHours(1));
        record.setUpdateTime(LocalDateTime.now().minusHours(1));
        return record;
    }
}
