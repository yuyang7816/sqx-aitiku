package com.sqx.app.base.messages.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.messages.domain.vo.AppMessageItemVO;
import com.sqx.app.base.messages.domain.vo.MessagesVo;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.bo.MessagesInfoBo;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.bo.MessagesSaveBo;
import com.sqx.app.base.messages.domain.bo.MessagesUpdateBo;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.messages.service.MessagesService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 消息信息表 服务实现类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
@Service
public class MessagesServiceImpl implements MessagesService {

    @Resource
    private IMessagesService iMessagesService;

    @Override
    public void saveInfo(MessagesSaveBo messagesSaveBo) {
        Messages messages = new Messages();
        BeanUtils.copyProperties(messagesSaveBo, messages);
        messages.setIsRead(resolveIsRead(messagesSaveBo.getIsRead()));

        iMessagesService.save(messages);
    }

    @Override
    public IPage<MessagesVo> pageInfo(MessagesPageBo messagesPageBo) {
        Page<Messages> initPage = PageUtil.initPage(messagesPageBo.getPage(), messagesPageBo.getLimit());

        IPage<MessagesVo> voIPage = iMessagesService.pageInfo(initPage, messagesPageBo);
        return voIPage;
    }

    @Override
    public IPage<AppMessageItemVO> pageAppMessages(Long userId, Long page, Long limit, Boolean read) {
        Page<Messages> initPage = PageUtil.initPage(page, limit);

        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getUserId, userId);
        if (read != null) {
            wrapper.eq(Messages::getIsRead, Boolean.TRUE.equals(read) ? 1 : 0);
        }
        wrapper.orderByDesc(Messages::getCreateTime);

        IPage<Messages> entityPage = iMessagesService.page(initPage, wrapper);
        List<AppMessageItemVO> records = entityPage.getRecords().stream()
                .map(this::toAppMessageItemVO)
                .collect(Collectors.toList());

        return PageUtil.copyPage(entityPage, records);
    }

    @Override
    public void markAllAppMessagesRead(Long userId) {
        UpdateWrapper<Messages> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("is_read", 0)
                .set("is_read", 1);
        iMessagesService.update(new Messages(), wrapper);
    }

    AppMessageItemVO toAppMessageItemVO(Messages message) {
        AppMessageItemVO vo = new AppMessageItemVO();
        vo.setMessageId(message.getId());
        vo.setId(resolveJumpId(message));
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setTime(formatMessageTime(message.getCreateTime()));
        vo.setRead(Objects.equals(message.getIsRead(), 1));
        vo.setType(message.getType());
        vo.setDataId(message.getDataId());
        vo.setImageUrls(message.getImageUrls());
        vo.setUrl(message.getUrl());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    /**
     * 跳转标识：优先使用 url 字段（管理端配置 notice-review 等），否则回退 msg-{主键}
     */
    String resolveJumpId(Messages message) {
        if (message == null) {
            return "";
        }
        if (StringUtils.isNotBlank(message.getUrl())) {
            return message.getUrl().trim();
        }
        if (message.getId() != null) {
            return "msg-" + message.getId();
        }
        return "";
    }

    /**
     * 与 uniapp notice.vue mock 一致的相对时间文案
     */
    String formatMessageTime(LocalDateTime createTime) {
        if (createTime == null) {
            return "";
        }
        LocalDateTime now = LocalDateTime.now();
        if (createTime.toLocalDate().equals(now.toLocalDate())) {
            long minutes = Duration.between(createTime, now).toMinutes();
            if (minutes >= 0 && minutes < 1) {
                return "刚刚";
            }
            if (minutes >= 0 && minutes < 60) {
                return minutes + "分钟前";
            }
            return "今天 " + createTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        if (createTime.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "昨天 " + createTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public MessagesVo info(MessagesInfoBo messagesInfoBo) {
        Messages messages = iMessagesService.getById(messagesInfoBo.getId());
        if (Objects.isNull(messages)) {
            throw new BizException("信息不存在");
        }

        MessagesVo vo = new MessagesVo();
        BeanUtils.copyProperties(messages, vo);
        return vo;
    }

    @Override
    public void updateInfo(MessagesUpdateBo messagesUpdateBo) {
        Messages messages = new Messages();
        BeanUtils.copyProperties(messagesUpdateBo, messages);
        if (messagesUpdateBo.getIsRead() != null) {
            messages.setIsRead(resolveIsRead(messagesUpdateBo.getIsRead()));
        }

        iMessagesService.updateById(messages);
    }

    /** 管理端 BO 使用 Boolean，实体 is_read 为 0/1 */
    Integer resolveIsRead(Boolean isRead) {
        if (isRead == null) {
            return 0;
        }
        return Boolean.TRUE.equals(isRead) ? 1 : 0;
    }

    @Override
    public void delete(MessagesInfoBo messagesInfoBo) {
        iMessagesService.removeById(messagesInfoBo.getId());
    }
}
