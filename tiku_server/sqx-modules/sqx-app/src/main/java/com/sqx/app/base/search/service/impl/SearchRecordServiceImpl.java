package com.sqx.app.base.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.search.constants.SearchRecordConstant;
import com.sqx.app.base.search.domain.bo.AppSearchRecordUpsertBO;
import com.sqx.app.base.search.domain.vo.AppSearchRecordItemVO;
import com.sqx.app.base.search.service.SearchRecordService;
import com.sqx.app.base.search.support.SearchRecordTimeSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.UserSearchRecord;
import com.sqx.system.db.service.IUserSearchRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户端搜题记录：按 userId + title 去重，最多保留 20 条
 */
@Service
public class SearchRecordServiceImpl implements SearchRecordService {

    private static final Set<String> ALLOWED_SOURCES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            SearchRecordConstant.SOURCE_PHOTO,
            SearchRecordConstant.SOURCE_MANUAL,
            SearchRecordConstant.SOURCE_AI
    )));

    @Resource
    private IUserSearchRecordService iUserSearchRecordService;

    @Override
    public List<AppSearchRecordItemVO> listRecords(Long userId) {
        LambdaQueryWrapper<UserSearchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSearchRecord::getUserId, userId)
                .orderByDesc(UserSearchRecord::getUpdateTime)
                .last("LIMIT " + SearchRecordConstant.MAX_RECORDS);
        return iUserSearchRecordService.list(wrapper).stream()
                .map(this::toItemVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppSearchRecordItemVO upsertRecord(Long userId, AppSearchRecordUpsertBO upsert) {
        String title = normalizeTitle(upsert.getTitle());
        String source = normalizeSource(upsert.getSource());
        String imageUrl = normalizeImageUrl(upsert.getImageUrl());

        UserSearchRecord record = findByUserAndTitle(userId, title);
        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            record = new UserSearchRecord();
            record.setUserId(userId);
            record.setTitle(title);
            record.setSource(source);
            record.setImageUrl(imageUrl);
            record.setCreateTime(now);
            record.setUpdateTime(now);
            iUserSearchRecordService.save(record);
        } else {
            record.setSource(source);
            if (StringUtils.isNotBlank(imageUrl)) {
                record.setImageUrl(imageUrl);
            } else if (StringUtils.isBlank(record.getImageUrl())) {
                record.setImageUrl(null);
            }
            record.setUpdateTime(now);
            iUserSearchRecordService.updateById(record);
        }
        trimOverflowRecords(userId);
        return toItemVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearRecords(Long userId) {
        LambdaQueryWrapper<UserSearchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSearchRecord::getUserId, userId);
        iUserSearchRecordService.remove(wrapper);
    }

    UserSearchRecord findByUserAndTitle(Long userId, String title) {
        LambdaQueryWrapper<UserSearchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSearchRecord::getUserId, userId)
                .eq(UserSearchRecord::getTitle, title)
                .last("LIMIT 1");
        return iUserSearchRecordService.getOne(wrapper);
    }

    void trimOverflowRecords(Long userId) {
        LambdaQueryWrapper<UserSearchRecord> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(UserSearchRecord::getUserId, userId);
        long total = iUserSearchRecordService.count(countWrapper);
        if (total <= SearchRecordConstant.MAX_RECORDS) {
            return;
        }
        int overflow = (int) (total - SearchRecordConstant.MAX_RECORDS);
        LambdaQueryWrapper<UserSearchRecord> oldestWrapper = new LambdaQueryWrapper<>();
        oldestWrapper.eq(UserSearchRecord::getUserId, userId)
                .orderByAsc(UserSearchRecord::getUpdateTime)
                .last("LIMIT " + overflow);
        List<UserSearchRecord> oldest = iUserSearchRecordService.list(oldestWrapper);
        if (oldest.isEmpty()) {
            return;
        }
        List<Long> ids = oldest.stream()
                .map(UserSearchRecord::getId)
                .collect(Collectors.toList());
        iUserSearchRecordService.removeByIds(ids);
    }

    String normalizeTitle(String title) {
        String value = StringUtils.trim(title);
        if (StringUtils.isBlank(value)) {
            throw new BizException("题干预览不能为空");
        }
        return value;
    }

    String normalizeSource(String source) {
        String value = StringUtils.trim(source);
        if (!ALLOWED_SOURCES.contains(value)) {
            return SearchRecordConstant.SOURCE_PHOTO;
        }
        return value;
    }

    String normalizeImageUrl(String imageUrl) {
        return StringUtils.trimToNull(imageUrl);
    }

    AppSearchRecordItemVO toItemVO(UserSearchRecord record) {
        AppSearchRecordItemVO vo = new AppSearchRecordItemVO();
        vo.setTitle(record.getTitle());
        vo.setTime(SearchRecordTimeSupport.formatRecordTime(record.getUpdateTime()));
        vo.setSource(record.getSource());
        if (StringUtils.isNotBlank(record.getImageUrl())) {
            vo.setImageUrl(record.getImageUrl());
        }
        return vo;
    }
}
