package com.sqx.app.base.ai.support;

import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.bo.AppAiChatHistoryItemBO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 本地模板 AI 回复单元测试
 */
class AiChatReplySupportTest {

    @Test
    void buildReply_whenEquationFirstTurn_shouldReturnDemoReply() {
        AppAiChatBO bo = new AppAiChatBO();
        bo.setMessage("x + 3 = 2 怎么解");

        String reply = AiChatReplySupport.buildReply(bo);

        assertTrue(reply.contains("移项"));
    }

    @Test
    void buildReply_whenUserIntentReport_shouldReturnPlanReply() {
        AppAiChatBO bo = new AppAiChatBO();
        bo.setMessage("请根据我的报告，帮我拆成今晚 30 分钟可执行的复习计划。");

        String reply = AiChatReplySupport.buildReply(bo);

        assertTrue(reply.contains("复习计划"));
    }

    @Test
    void buildReply_whenMultiTurnWhy_shouldReturnReasonReply() {
        AppAiChatHistoryItemBO user = new AppAiChatHistoryItemBO();
        user.setRole("user");
        user.setText("请帮我解答这道方程题：x + 3 = 2 怎么解");
        AppAiChatHistoryItemBO ai = new AppAiChatHistoryItemBO();
        ai.setRole("ai");
        ai.setContent("先移项得到 x = -1");

        AppAiChatBO bo = new AppAiChatBO();
        bo.setTopic("x + 3 = 2");
        bo.setMessage("为什么要变号？");
        bo.setHistory(Arrays.asList(user, ai));

        String reply = AiChatReplySupport.buildReply(bo);

        assertTrue(reply.contains("等式"));
    }

    @Test
    void buildReply_whenTopicOnly_shouldMentionTopic() {
        AppAiChatBO bo = new AppAiChatBO();
        bo.setTopic("勾股定理应用题");
        bo.setMessage("讲解一下勾股定理应用题");
        bo.setHistory(Collections.emptyList());

        String reply = AiChatReplySupport.buildReply(bo);

        assertTrue(reply.contains("勾股定理"));
    }
}
