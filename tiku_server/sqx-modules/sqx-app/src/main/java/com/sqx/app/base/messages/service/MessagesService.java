package com.sqx.app.base.messages.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.messages.domain.bo.MessagesInfoBo;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.bo.MessagesSaveBo;
import com.sqx.app.base.messages.domain.bo.MessagesUpdateBo;
import com.sqx.app.base.messages.domain.vo.AppMessageItemVO;
import com.sqx.app.base.messages.domain.vo.MessagesVo;

/**
 * 消息信息表 服务类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
public interface MessagesService {

    void saveInfo(MessagesSaveBo messagesSaveBo);

    IPage<MessagesVo> pageInfo(MessagesPageBo messagesPageBo);

    /**
     * 用户端分页消息列表（GET /app/messages）
     *
     * @param userId 当前登录用户 ID
     * @param page   页码
     * @param limit  每页条数
     * @param read   可选，按已读状态筛选
     */
    IPage<AppMessageItemVO> pageAppMessages(Long userId, Long page, Long limit, Boolean read);

    /**
     * 用户端全部标记已读（POST /app/messages/read-all）
     *
     * @param userId 当前登录用户 ID
     */
    void markAllAppMessagesRead(Long userId);

    MessagesVo info(MessagesInfoBo messagesInfoBo);

    void updateInfo(MessagesUpdateBo messagesUpdateBo);

    void delete(MessagesInfoBo messagesInfoBo);
}
