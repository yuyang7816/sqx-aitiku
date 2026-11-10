package com.sqx.app.base.question.detail.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.question.list.constants.QuestionAnswerStatus;
import com.sqx.app.base.question.list.domain.vo.AppQuestionListItemVO;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.UserQuestionAnswer;
import com.sqx.system.db.service.IQuestionService;
import com.sqx.system.db.service.IUserQuestionAnswerService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/question/{id} 单题详情单元测试
 */
@ExtendWith(MockitoExtension.class)
class QuestionDetailServiceImplTest {

    @Mock
    private IQuestionService iQuestionService;

    @Mock
    private IUserQuestionAnswerService iUserQuestionAnswerService;

    @InjectMocks
    private QuestionDetailServiceImpl questionDetailService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, Question.class);
        TableInfoHelper.initTableInfo(assistant, UserQuestionAnswer.class);
    }

    @Test
    void getQuestionDetail_shouldReturnFullDetailByNumericId() {
        Question question = buildQuestion(101L, "rational-numbers-q01");
        when(iQuestionService.getById(101L)).thenReturn(question);
        when(iUserQuestionAnswerService.getOne(any(Wrapper.class))).thenReturn(null);

        AppQuestionListItemVO vo = questionDetailService.getQuestionDetail(1L, "101");

        assertNotNull(vo);
        assertEquals(101L, vo.getQuestionId());
        assertEquals("rational-numbers-q01", vo.getQuestionCode());
        assertEquals("下列各数中，属于负数的是（ ）", vo.getTitle());
        assertEquals(QuestionAnswerStatus.TYPE_TODO, vo.getType());
        assertEquals(2, vo.getOptions().size());
        assertEquals(1, vo.getTags().size());
    }

    @Test
    void getQuestionDetail_shouldReturnDetailByQuestionCode() {
        Question question = buildQuestion(102L, "rational-numbers-q02");
        when(iQuestionService.getOne(any(Wrapper.class))).thenReturn(question);
        when(iUserQuestionAnswerService.getOne(any(Wrapper.class))).thenReturn(buildAnswer(1L, 102L, QuestionAnswerStatus.CORRECT_YES));

        AppQuestionListItemVO vo = questionDetailService.getQuestionDetail(1L, "rational-numbers-q02");

        assertEquals(102L, vo.getQuestionId());
        assertEquals(QuestionAnswerStatus.TYPE_RIGHT, vo.getType());
        assertEquals(QuestionAnswerStatus.LABEL_RIGHT, vo.getStatus());
        verify(iQuestionService, never()).getById(any());
    }

    @Test
    void getQuestionDetail_shouldThrowWhenQuestionNotFound() {
        when(iQuestionService.getById(999L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> questionDetailService.getQuestionDetail(1L, "999"));
        assertEquals(AppError.INFO_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getQuestionDetail_shouldThrowWhenQuestionDisabled() {
        Question question = buildQuestion(101L, "rational-numbers-q01");
        question.setState(0);
        when(iQuestionService.getById(101L)).thenReturn(question);

        BizException ex = assertThrows(BizException.class,
                () -> questionDetailService.getQuestionDetail(1L, "101"));
        assertEquals(AppError.INFO_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getQuestionDetail_shouldThrowWhenIdBlank() {
        assertThrows(BizException.class, () -> questionDetailService.getQuestionDetail(1L, "  "));
    }

    private Question buildQuestion(Long id, String code) {
        Question question = new Question();
        question.setId(id);
        question.setQuestionCode(code);
        question.setTitle("下列各数中，属于负数的是（ ）");
        question.setQuestionType("单选题");
        question.setDifficulty("简单");
        question.setRightAnswer("A");
        question.setAnalysis("解析内容");
        question.setSortOrder(1);
        question.setState(1);
        question.setOptionsJson("[{\"label\":\"A\",\"value\":\"-3\"},{\"label\":\"B\",\"value\":\"0\"}]");
        question.setTagsJson("[\"#有理数\"]");
        return question;
    }

    private UserQuestionAnswer buildAnswer(Long userId, Long questionId, int isCorrect) {
        UserQuestionAnswer answer = new UserQuestionAnswer();
        answer.setUserId(userId);
        answer.setQuestionId(questionId);
        answer.setIsCorrect(isCorrect);
        return answer;
    }
}
