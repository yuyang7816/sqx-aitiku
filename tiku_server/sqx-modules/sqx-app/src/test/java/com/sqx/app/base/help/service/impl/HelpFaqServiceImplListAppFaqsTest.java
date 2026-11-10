package com.sqx.app.base.help.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.sqx.app.base.help.db.service.IHelpFaqService;
import com.sqx.app.base.help.domain.entity.HelpFaq;
import com.sqx.app.base.help.domain.vo.AppHelpFaqItemVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/help/faq/list 单元测试
 */
@ExtendWith(MockitoExtension.class)
class HelpFaqServiceImplListAppFaqsTest {

    @Mock
    private IHelpFaqService iHelpFaqService;

    @InjectMocks
    private HelpFaqServiceImpl helpFaqService;

    @Test
    void listAppFaqs_shouldReturnEnabledFaqsOrderedBySort() {
        HelpFaq first = buildFaq(1L, "如何开通会员？", "进入会员中心选择套餐支付即可。", 1, 1);
        HelpFaq second = buildFaq(2L, "错题如何同步？", "交卷后自动收录。", 2, 1);
        when(iHelpFaqService.list(any(Wrapper.class))).thenReturn(Arrays.asList(first, second));

        List<AppHelpFaqItemVO> result = helpFaqService.listAppFaqs();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("如何开通会员？", result.get(0).getQuestion());
        assertEquals("进入会员中心选择套餐支付即可。", result.get(0).getAnswer());

        verify(iHelpFaqService).list(any(Wrapper.class));
    }

    @Test
    void listAppFaqs_whenNoRecords_shouldReturnEmptyList() {
        when(iHelpFaqService.list(any(Wrapper.class))).thenReturn(Collections.emptyList());

        List<AppHelpFaqItemVO> result = helpFaqService.listAppFaqs();

        assertTrue(result.isEmpty());
    }

    @Test
    void listAppFaqs_shouldSkipBlankQuestionOrAnswer() {
        HelpFaq valid = buildFaq(1L, "有效问题", "有效答案", 1, 1);
        HelpFaq blankAnswer = buildFaq(2L, "无答案", "  ", 2, 1);
        when(iHelpFaqService.list(any(Wrapper.class))).thenReturn(Arrays.asList(valid, blankAnswer));

        List<AppHelpFaqItemVO> result = helpFaqService.listAppFaqs();

        assertEquals(1, result.size());
        assertEquals("有效问题", result.get(0).getQuestion());
    }

    private HelpFaq buildFaq(Long id, String question, String answer, Integer sort, Integer state) {
        HelpFaq faq = new HelpFaq();
        faq.setId(id);
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setSort(sort);
        faq.setState(state);
        return faq;
    }
}
