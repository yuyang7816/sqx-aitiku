package com.sqx.app.base.kefuChat.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.base.kefuChat.domain.entity.KefuChatMessage;
import com.sqx.app.base.kefuChat.domain.vo.KefuChatSessionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客服 IM 消息 Mapper
 */
public interface KefuChatMessageMapper extends BaseMapper<KefuChatMessage> {

    /**
     * 管理端会话列表（按用户聚合）
     */
    List<KefuChatSessionVO> listSessions(@Param("limit") int limit);
}
