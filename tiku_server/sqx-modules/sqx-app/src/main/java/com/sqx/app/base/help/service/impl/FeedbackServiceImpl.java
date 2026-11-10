package com.sqx.app.base.help.service.impl;

import com.sqx.app.base.help.constants.FeedbackConstant;
import com.sqx.app.base.help.db.service.IUserFeedbackService;
import com.sqx.app.base.help.domain.bo.FeedbackSubmitBo;
import com.sqx.app.base.help.domain.entity.UserFeedback;
import com.sqx.app.base.help.domain.vo.AppFeedbackSubmitResultVO;
import com.sqx.app.base.help.service.FeedbackService;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private static final DateTimeFormatter TICKET_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final Set<String> ALLOWED_CATEGORIES = new HashSet<>(
            Arrays.asList("feature", "account", "search", "other")
    );

    @Resource
    private IUserFeedbackService iUserFeedbackService;

    @Override
    public AppFeedbackSubmitResultVO submit(Long userId, FeedbackSubmitBo submitBo) {
        String category = StringUtils.trimToEmpty(submitBo.getCategory());
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new BizException("请选择有效的反馈类型");
        }

        String contactPhone = StringUtils.trimToEmpty(submitBo.getContactPhone());
        if (StringUtils.isNotBlank(contactPhone) && !contactPhone.matches("^1\\d{10}$")) {
            throw new BizException("请输入正确的手机号");
        }

        UserFeedback entity = new UserFeedback();
        entity.setTicketNo(buildTicketNo());
        entity.setUserId(userId);
        entity.setCategory(category);
        entity.setContent(StringUtils.trimToEmpty(submitBo.getContent()));
        entity.setContactPhone(contactPhone);
        entity.setImageUrls(joinImages(submitBo.getImages()));
        entity.setStatus(FeedbackConstant.STATUS_PENDING);

        iUserFeedbackService.save(entity);

        AppFeedbackSubmitResultVO vo = new AppFeedbackSubmitResultVO();
        vo.setTicketNo(entity.getTicketNo());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    private String buildTicketNo() {
        return "FB" + LocalDateTime.now().format(TICKET_TIME_FORMAT);
    }

    private String joinImages(java.util.List<String> images) {
        if (CollectionUtils.isEmpty(images)) {
            return null;
        }
        return images.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.joining(","));
    }
}
