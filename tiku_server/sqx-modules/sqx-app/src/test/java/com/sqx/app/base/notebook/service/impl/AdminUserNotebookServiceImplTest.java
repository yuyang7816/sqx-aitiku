package com.sqx.app.base.notebook.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.notebook.db.mapper.AdminUserNotebookMapper;
import com.sqx.app.base.notebook.domain.bo.AdminUserNotebookPageBo;
import com.sqx.app.base.notebook.domain.vo.AppNotebookItemVO;
import com.sqx.app.base.notebook.domain.vo.AdminUserNotebookItemVo;
import com.sqx.app.base.notebook.support.NotebookItemAssembler;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IUserQuestionWrongService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserNotebookServiceImplTest {

    @Mock
    private IUserQuestionWrongService iUserQuestionWrongService;

    @Mock
    private IUserService iUserService;

    @Mock
    private NotebookItemAssembler notebookItemAssembler;

    @Mock
    private AdminUserNotebookMapper adminUserNotebookMapper;

    @InjectMocks
    private AdminUserNotebookServiceImpl adminUserNotebookService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, UserQuestionWrong.class);
        TableInfoHelper.initTableInfo(assistant, Question.class);
        TableInfoHelper.initTableInfo(assistant, UserEntity.class);
    }

    @Test
    void pageNotebook_shouldReturnItemsForUserId() {
        UserQuestionWrong wrong = buildWrong(1L, 3L, 101L);
        Question question = buildQuestion(101L);

        Page<UserQuestionWrong> wrongPage = new Page<>(1, 20);
        wrongPage.setRecords(Collections.singletonList(wrong));
        wrongPage.setTotal(1);

        UserEntity user = new UserEntity();
        user.setUserId(3L);
        user.setUserName("test_user");
        user.setNickname("测试");
        user.setPhone("13800138000");
        user.setGradeShort("初三");

        when(iUserQuestionWrongService.page(any(Page.class), any())).thenReturn(wrongPage);
        when(iUserService.listByIds(any())).thenReturn(Collections.singletonList(user));
        when(notebookItemAssembler.loadQuestionMap(any(), eq(false)))
                .thenReturn(Collections.singletonMap(101L, question));
        when(notebookItemAssembler.toItemVO(wrong, question)).thenReturn(buildNotebookItem(wrong, question));

        AdminUserNotebookPageBo pageBo = new AdminUserNotebookPageBo();
        pageBo.setPage(1L);
        pageBo.setLimit(20L);
        pageBo.setUserId(3L);

        IPage<AdminUserNotebookItemVo> result = adminUserNotebookService.pageNotebook(pageBo);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(3L, result.getRecords().get(0).getUserId());
        assertEquals("test_user", result.getRecords().get(0).getUserName());
    }

    private UserQuestionWrong buildWrong(Long id, Long userId, Long questionId) {
        UserQuestionWrong wrong = new UserQuestionWrong();
        wrong.setId(id);
        wrong.setUserId(userId);
        wrong.setQuestionId(questionId);
        wrong.setSubject("数学");
        wrong.setUserAnswer("A");
        wrong.setWrongCount(1);
        wrong.setMastered(UserQuestionWrong.MASTERED_NO);
        wrong.setLastPracticeAt(LocalDateTime.of(2026, 8, 16, 10, 0));
        wrong.setCreateTime(LocalDateTime.of(2026, 8, 16, 9, 0));
        wrong.setUpdateTime(LocalDateTime.of(2026, 8, 16, 10, 0));
        return wrong;
    }

    private Question buildQuestion(Long id) {
        Question question = new Question();
        question.setId(id);
        question.setQuestionCode("q-" + id);
        question.setTitle("测试题干");
        question.setRightAnswer("C");
        question.setAnalysis("解析");
        question.setOptionsJson("[{\"label\":\"A\",\"value\":\"1\"}]");
        question.setTagsJson("[\"#标签\"]");
        question.setState(1);
        return question;
    }

    private AppNotebookItemVO buildNotebookItem(UserQuestionWrong wrong, Question question) {
        AppNotebookItemVO vo = new AppNotebookItemVO();
        vo.setId(question.getQuestionCode());
        vo.setNotebookId(wrong.getId());
        vo.setQuestionId(question.getId());
        vo.setQuestionCode(question.getQuestionCode());
        vo.setSubject(wrong.getSubject());
        vo.setTitle(question.getTitle());
        vo.setQuestion(question.getTitle());
        vo.setMyAnswer(wrong.getUserAnswer());
        vo.setWrong(wrong.getUserAnswer());
        vo.setRightAnswer(question.getRightAnswer());
        vo.setRight(question.getRightAnswer());
        vo.setAnalysis(question.getAnalysis());
        vo.setWrongCount(wrong.getWrongCount());
        vo.setMastered(false);
        vo.setLastPracticeAt("2026-08-16 10:00:00");
        return vo;
    }
}
