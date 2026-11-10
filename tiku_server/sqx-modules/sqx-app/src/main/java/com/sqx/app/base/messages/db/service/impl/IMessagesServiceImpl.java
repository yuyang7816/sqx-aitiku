package com.sqx.app.base.messages.db.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.messages.db.mapper.MessagesMapper;
import com.sqx.app.base.messages.db.service.IMessagesService;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.messages.domain.vo.MessagesVo;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 消息信息表 服务实现类
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
@Service
public class IMessagesServiceImpl extends ServiceImpl<MessagesMapper, Messages> implements IMessagesService {

    @Override
    public IPage<MessagesVo> pageInfo(Page<Messages> initPage, MessagesPageBo messagesPageBo) {
        return this.baseMapper.pageInfo(initPage, messagesPageBo);
    }
}
