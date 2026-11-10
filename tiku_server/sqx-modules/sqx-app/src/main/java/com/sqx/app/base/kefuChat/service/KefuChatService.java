package com.sqx.app.base.kefuChat.service;

import com.sqx.app.base.kefuChat.domain.vo.KefuChatMessageVO;
import com.sqx.app.base.kefuChat.domain.vo.KefuChatSessionVO;

import java.util.List;

/**
 * 客服在线 IM 服务
 */
public interface KefuChatService {

    List<KefuChatMessageVO> listMessages(Long userId, int limit);

    List<KefuChatMessageVO> listStaffMessages(Long userId, int limit);

    KefuChatMessageVO sendUserMessage(Long userId, String content);

    KefuChatMessageVO sendStaffReply(Long userId, String content);

    int countUnreadForUser(Long userId);

    int countUnreadForStaff();

    void markReadForUser(Long userId);

    void markReadForStaff(Long userId);

    List<KefuChatSessionVO> listSessions(int limit);
}
