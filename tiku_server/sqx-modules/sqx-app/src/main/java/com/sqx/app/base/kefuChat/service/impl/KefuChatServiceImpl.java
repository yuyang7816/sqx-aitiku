package com.sqx.app.base.kefuChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.customerConfig.domain.vo.CustomerConfigVO;
import com.sqx.app.base.customerConfig.service.CustomerConfigService;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.kefuChat.db.mapper.KefuChatMessageMapper;
import com.sqx.app.base.kefuChat.domain.entity.KefuChatMessage;
import com.sqx.app.base.kefuChat.domain.vo.KefuChatMessageVO;
import com.sqx.app.base.kefuChat.domain.vo.KefuChatSessionVO;
import com.sqx.app.base.kefuChat.service.KefuChatService;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KefuChatServiceImpl implements KefuChatService {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 200;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Resource
    private KefuChatMessageMapper kefuChatMessageMapper;

    @Resource
    private CustomerConfigService customerConfigService;

    @Override
    public List<KefuChatMessageVO> listMessages(Long userId, int limit) {
        return listMessagesByViewer(userId, limit, "user");
    }

    @Override
    public List<KefuChatMessageVO> listStaffMessages(Long userId, int limit) {
        return listMessagesByViewer(userId, limit, "staff");
    }

    private List<KefuChatMessageVO> listMessagesByViewer(Long userId, int limit, String viewerRole) {
        assertUserId(userId);
        int safeLimit = normalizeLimit(limit);

        LambdaQueryWrapper<KefuChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KefuChatMessage::getUserId, userId)
                .orderByAsc(KefuChatMessage::getCreateTime)
                .last("LIMIT " + safeLimit);

        List<KefuChatMessage> rows = kefuChatMessageMapper.selectList(wrapper);
        return rows.stream()
                .map(row -> toMessageVO(row, viewerRole))
                .collect(Collectors.toList());
    }

    @Override
    public KefuChatMessageVO sendUserMessage(Long userId, String content) {
        assertUserId(userId);
        assertImEnabled();
        String text = normalizeContent(content);

        KefuChatMessage entity = buildMessage(userId, "user", text, 0);
        kefuChatMessageMapper.insert(entity);
        return toMessageVO(entity, "user");
    }

    @Override
    public KefuChatMessageVO sendStaffReply(Long userId, String content) {
        assertUserId(userId);
        String text = normalizeContent(content);

        KefuChatMessage entity = buildMessage(userId, "staff", text, 0);
        kefuChatMessageMapper.insert(entity);
        return toMessageVO(entity, "staff");
    }

    @Override
    public int countUnreadForUser(Long userId) {
        assertUserId(userId);
        LambdaQueryWrapper<KefuChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KefuChatMessage::getUserId, userId)
                .eq(KefuChatMessage::getSenderRole, "staff")
                .eq(KefuChatMessage::getIsRead, 0);
        Long count = kefuChatMessageMapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    @Override
    public int countUnreadForStaff() {
        LambdaQueryWrapper<KefuChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KefuChatMessage::getSenderRole, "user")
                .eq(KefuChatMessage::getIsRead, 0);
        Long count = kefuChatMessageMapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    @Override
    public void markReadForUser(Long userId) {
        assertUserId(userId);
        LambdaUpdateWrapper<KefuChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(KefuChatMessage::getUserId, userId)
                .eq(KefuChatMessage::getSenderRole, "staff")
                .eq(KefuChatMessage::getIsRead, 0)
                .set(KefuChatMessage::getIsRead, 1);
        kefuChatMessageMapper.update(null, wrapper);
    }

    @Override
    public void markReadForStaff(Long userId) {
        assertUserId(userId);
        LambdaUpdateWrapper<KefuChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(KefuChatMessage::getUserId, userId)
                .eq(KefuChatMessage::getSenderRole, "user")
                .eq(KefuChatMessage::getIsRead, 0)
                .set(KefuChatMessage::getIsRead, 1);
        kefuChatMessageMapper.update(null, wrapper);
    }

    @Override
    public List<KefuChatSessionVO> listSessions(int limit) {
        int safeLimit = normalizeLimit(limit);
        List<KefuChatSessionVO> sessions = kefuChatMessageMapper.listSessions(safeLimit);
        return sessions == null ? Collections.emptyList() : sessions;
    }

    private KefuChatMessage buildMessage(Long userId, String senderRole, String content, int isRead) {
        KefuChatMessage entity = new KefuChatMessage();
        entity.setUserId(userId);
        entity.setSenderRole(senderRole);
        entity.setContent(content);
        entity.setIsRead(isRead);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return entity;
    }

    private KefuChatMessageVO toMessageVO(KefuChatMessage entity, String viewerRole) {
        KefuChatMessageVO vo = new KefuChatMessageVO();
        vo.setId(entity.getId());
        vo.setSenderRole(entity.getSenderRole());
        vo.setContent(entity.getContent());
        vo.setTime(formatTime(entity.getCreateTime()));
        boolean read = entity.getIsRead() != null && entity.getIsRead() == 1;
        if ("user".equals(viewerRole) && "staff".equals(entity.getSenderRole())) {
            vo.setRead(read);
        } else if ("staff".equals(viewerRole) && "user".equals(entity.getSenderRole())) {
            vo.setRead(read);
        } else {
            vo.setRead(true);
        }
        return vo;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(TIME_FMT);
    }

    private void assertImEnabled() {
        CustomerConfigVO config = customerConfigService.getCustomerConfig();
        if (config == null || !Boolean.TRUE.equals(config.getImEnabled())) {
            throw new BizException("在线 IM 客服未开启");
        }
    }

    private void assertUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
    }

    private String normalizeContent(String content) {
        String text = StringUtils.trimToEmpty(content);
        if (text.isEmpty()) {
            throw new BizException("消息内容不能为空");
        }
        if (text.length() > 500) {
            throw new BizException("消息内容不超过 500 字");
        }
        return text;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
