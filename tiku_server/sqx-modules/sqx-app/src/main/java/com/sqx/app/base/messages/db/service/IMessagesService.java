package com.sqx.app.base.messages.db.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.messages.domain.vo.MessagesVo;

/**
 * <p>
 * 消息信息表 服务类
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
public interface IMessagesService extends IService<Messages> {

    IPage<MessagesVo> pageInfo(Page<Messages> initPage, MessagesPageBo messagesPageBo);
}
