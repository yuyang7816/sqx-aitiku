package com.sqx.app.base.notebook.support;

import com.sqx.app.base.notebook.domain.vo.AppNotebookItemVO;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.entity.UserQuestionWrong;
import com.sqx.system.db.service.IQuestionChapterService;
import com.sqx.system.db.service.IQuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotebookItemAssemblerTest {

    @Mock
    private IQuestionService iQuestionService;

    @Mock
    private IQuestionChapterService iQuestionChapterService;

    @InjectMocks
    private NotebookItemAssembler notebookItemAssembler;

    private Question question;
    private UserQuestionWrong wrong;

    @BeforeEach
    void setUp() {
        question = new Question();
        question.setId(101L);
        question.setQuestionCode("rational-numbers-q02");
        question.setChapterId(1L);
        question.setTitle("下列各数中，属于负数的是？");
        question.setRightAnswer("C");
        question.setState(1);

        wrong = new UserQuestionWrong();
        wrong.setId(10L);
        wrong.setUserId(100L);
        wrong.setQuestionId(101L);
        wrong.setUserAnswer("B");
        wrong.setWrongCount(2);
        wrong.setMastered(UserQuestionWrong.MASTERED_NO);
    }

    @Test
    void toItemVO_whenStoredSubjectBlank_shouldUseChapterSubject() {
        wrong.setSubject(null);

        QuestionChapter chapter = new QuestionChapter();
        chapter.setId(1L);
        chapter.setSubject("数学");
        when(iQuestionChapterService.listByIds(anyCollection())).thenReturn(Collections.singletonList(chapter));

        Map<Long, String> chapterSubjectMap = notebookItemAssembler.loadChapterSubjectMap(Collections.singletonList(question));
        AppNotebookItemVO vo = notebookItemAssembler.toItemVO(wrong, question, chapterSubjectMap);

        assertEquals("数学", vo.getSubject());
    }

    @Test
    void toItemVO_whenStoredSubjectIsGeneric_shouldUseChapterSubject() {
        wrong.setSubject("章节练习");

        QuestionChapter chapter = new QuestionChapter();
        chapter.setId(1L);
        chapter.setSubject("数学");
        when(iQuestionChapterService.listByIds(anyCollection())).thenReturn(Collections.singletonList(chapter));

        Map<Long, String> chapterSubjectMap = notebookItemAssembler.loadChapterSubjectMap(Collections.singletonList(question));
        AppNotebookItemVO vo = notebookItemAssembler.toItemVO(wrong, question, chapterSubjectMap);

        assertEquals("数学", vo.getSubject());
    }

    @Test
    void toItemVO_whenStoredSubjectPresent_shouldKeepStoredSubject() {
        wrong.setSubject("数学 · 初一");

        AppNotebookItemVO vo = notebookItemAssembler.toItemVO(wrong, question, Collections.emptyMap());

        assertEquals("数学 · 初一", vo.getSubject());
    }
}
