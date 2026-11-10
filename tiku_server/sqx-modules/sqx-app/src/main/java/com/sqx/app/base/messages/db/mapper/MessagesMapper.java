package com.sqx.app.base.messages.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.entity.Messages;
import com.sqx.app.base.messages.domain.vo.MessagesVo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 消息信息表 Mapper 接口
 * </p>
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
public interface MessagesMapper extends BaseMapper<Messages> {

    IPage<MessagesVo> pageInfo(Page<Messages> initPage, @Param("params") MessagesPageBo messagesPageBo);
}
