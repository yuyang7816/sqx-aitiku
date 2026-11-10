package com.sqx.app.integration.m21;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqx.app.base.ai.controller.AppAiChatController;
import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.vo.AppAiChatResultVO;
import com.sqx.app.base.ai.domain.vo.AppAiQuotaVO;
import com.sqx.app.base.ai.service.AiChatService;
import com.sqx.app.base.analysis.controller.AppAnalysisTodayController;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisOverviewVO;
import com.sqx.app.base.analysis.domain.vo.AppAnalysisReportVO;
import com.sqx.app.base.analysis.service.AnalysisOverviewService;
import com.sqx.app.base.analysis.service.AnalysisReportService;
import com.sqx.app.base.analysis.service.AnalysisTodayService;
import com.sqx.app.base.collection.controller.AppCollectionController;
import com.sqx.app.base.collection.domain.bo.AppCollectionCreateBO;
import com.sqx.app.base.collection.domain.vo.AppCollectionItemVO;
import com.sqx.app.base.collection.domain.vo.AppCollectionToggleResultVO;
import com.sqx.app.base.collection.service.CollectionService;
import com.sqx.app.base.notebook.controller.AppNotebookController;
import com.sqx.app.base.notebook.domain.bo.AppNotebookCreateBO;
import com.sqx.app.base.notebook.domain.vo.AppNotebookAddResultVO;
import com.sqx.app.base.notebook.domain.vo.AppNotebookItemVO;
import com.sqx.app.base.notebook.service.NotebookService;
import com.sqx.app.base.ocr.controller.AppOcrSearchController;
import com.sqx.app.base.ocr.domain.bo.AppOcrSearchBO;
import com.sqx.app.base.ocr.domain.vo.AppOcrSearchResultVO;
import com.sqx.app.base.ocr.service.OcrSearchService;
import com.sqx.app.base.practice.controller.AppPracticeController;
import com.sqx.app.base.practice.domain.bo.AppPracticeSubmitAnswerBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeSubmitBO;
import com.sqx.app.base.practice.domain.vo.AppPracticeProgressVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeRecordItemVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeSubmitResultVO;
import com.sqx.app.base.practice.service.PracticeService;
import com.sqx.app.base.question.chapter.controller.AppQuestionChapterTreeController;
import com.sqx.app.base.question.chapter.domain.bo.AppQuestionChapterTreeQueryBO;
import com.sqx.app.base.question.chapter.domain.vo.AppQuestionChapterNodeVO;
import com.sqx.app.base.question.chapter.service.QuestionChapterTreeService;
import com.sqx.app.base.question.detail.controller.AppQuestionDetailController;
import com.sqx.app.base.question.detail.service.QuestionDetailService;
import com.sqx.app.base.question.list.controller.AppQuestionListController;
import com.sqx.app.base.question.list.domain.vo.AppQuestionListItemVO;
import com.sqx.app.base.question.list.service.QuestionListService;
import com.sqx.app.base.search.controller.AppSearchRecordController;
import com.sqx.app.base.search.domain.bo.AppSearchRecordUpsertBO;
import com.sqx.app.base.search.domain.vo.AppSearchRecordItemVO;
import com.sqx.app.base.search.service.SearchRecordService;
import com.sqx.common.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 模块二十一 21.2 缺口 API 集成测试（TC-M21-001~012）
 * <p>
 * MockMvc 直调 Controller + Mock Service，验证路径、鉴权属性与 Result 出参结构。
 */
@ExtendWith(MockitoExtension.class)
class M21GapApiIntegrationTest {

    private static final Long USER_ID = 1L;
    private static final String CHAPTER_CODE = "rational-numbers";
    private static final String QUESTION_CODE = "rational-numbers-q01";

    @Mock
    private QuestionChapterTreeService questionChapterTreeService;
    @Mock
    private QuestionListService questionListService;
    @Mock
    private QuestionDetailService questionDetailService;
    @Mock
    private PracticeService practiceService;
    @Mock
    private NotebookService notebookService;
    @Mock
    private CollectionService collectionService;
    @Mock
    private OcrSearchService ocrSearchService;
    @Mock
    private AiChatService aiChatService;
    @Mock
    private AnalysisOverviewService analysisOverviewService;
    @Mock
    private AnalysisReportService analysisReportService;
    @Mock
    private AnalysisTodayService analysisTodayService;
    @Mock
    private SearchRecordService searchRecordService;

    @InjectMocks
    private AppQuestionChapterTreeController chapterTreeController;
    @InjectMocks
    private AppQuestionListController questionListController;
    @InjectMocks
    private AppQuestionDetailController questionDetailController;
    @InjectMocks
    private AppPracticeController practiceController;
    @InjectMocks
    private AppNotebookController notebookController;
    @InjectMocks
    private AppCollectionController collectionController;
    @InjectMocks
    private AppOcrSearchController ocrSearchController;
    @InjectMocks
    private AppAiChatController aiChatController;
    @InjectMocks
    private AppAnalysisTodayController analysisController;
    @InjectMocks
    private AppSearchRecordController searchRecordController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        chapterTreeController,
                        questionListController,
                        questionDetailController,
                        practiceController,
                        notebookController,
                        collectionController,
                        ocrSearchController,
                        aiChatController,
                        analysisController,
                        searchRecordController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("TC-M21-001 GET /app/question/chapter/tree 章节树（匿名）")
    void tcM21001_chapterTree() throws Exception {
        AppQuestionChapterNodeVO node = new AppQuestionChapterNodeVO();
        node.setCode(CHAPTER_CODE);
        node.setTitle("第一章：有理数");
        node.setQuestionCount(45);
        when(questionChapterTreeService.getChapterTree(any(AppQuestionChapterTreeQueryBO.class)))
                .thenReturn(Collections.singletonList(node));

        mockMvc.perform(get("/app/question/chapter/tree")
                        .param("grade", "junior_3")
                        .param("subject", "math")
                        .param("version", "renjiao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].code").value(CHAPTER_CODE))
                .andExpect(jsonPath("$.data[0].title").value("第一章：有理数"))
                .andExpect(jsonPath("$.data[0].questionCount").value(45));
    }

    @Test
    @DisplayName("TC-M21-002 GET /app/question/list 题目列表（需 JWT）")
    void tcM21002_questionList() throws Exception {
        AppQuestionListItemVO item = buildQuestionListItem();
        Page<AppQuestionListItemVO> page = new Page<>(1, 50);
        page.setRecords(Collections.singletonList(item));
        page.setTotal(1);
        when(questionListService.pageQuestionList(eq(USER_ID), any())).thenReturn(page);

        mockMvc.perform(get("/app/question/list")
                        .requestAttr("userId", USER_ID)
                        .param("chapterId", CHAPTER_CODE)
                        .param("mode", "all")
                        .param("page", "1")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].questionCode").value(QUESTION_CODE))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("TC-M21-003 GET /app/question/{id} 题目详情（需 JWT）")
    void tcM21003_questionDetail() throws Exception {
        when(questionDetailService.getQuestionDetail(eq(USER_ID), eq(QUESTION_CODE)))
                .thenReturn(buildQuestionListItem());

        mockMvc.perform(get("/app/question/" + QUESTION_CODE)
                        .requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.questionCode").value(QUESTION_CODE))
                .andExpect(jsonPath("$.data.title").exists());
    }

    @Test
    @DisplayName("TC-M21-004 POST /app/practice/submit 练习提交（需 JWT）")
    void tcM21004_practiceSubmit() throws Exception {
        AppPracticeSubmitResultVO result = new AppPracticeSubmitResultVO();
        result.setRecordCode("PR-20260813-001");
        result.setSavedCount(1);
        result.setTotalCount(1);
        result.setCorrectCount(1);
        result.setWrongCount(0);
        result.setScore(100);
        result.setDurationSeconds(60);
        result.setSourceMode("all");
        AppPracticeProgressVO progress = new AppPracticeProgressVO();
        progress.setChapterId(CHAPTER_CODE);
        progress.setPercent(85);
        progress.setDoneCount(3);
        progress.setTotalCount(45);
        result.setProgress(progress);
        when(practiceService.submitPractice(eq(USER_ID), any(AppPracticeSubmitBO.class))).thenReturn(result);

        AppPracticeSubmitAnswerBO answer = new AppPracticeSubmitAnswerBO();
        answer.setQuestionCode(QUESTION_CODE);
        answer.setUserAnswer("A");
        AppPracticeSubmitBO submit = new AppPracticeSubmitBO();
        submit.setChapterId(CHAPTER_CODE);
        submit.setAnswers(Collections.singletonList(answer));
        submit.setDurationSeconds(60);
        submit.setSourceMode("all");

        mockMvc.perform(post("/app/practice/submit")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.recordCode").value("PR-20260813-001"))
                .andExpect(jsonPath("$.data.progress.chapterId").value(CHAPTER_CODE));
    }

    @Test
    @DisplayName("TC-M21-005 GET /app/practice/records 练习记录（需 JWT）")
    void tcM21005_practiceRecords() throws Exception {
        AppPracticeRecordItemVO record = new AppPracticeRecordItemVO();
        record.setRecordCode("PR-20260813-001");
        record.setChapterId(CHAPTER_CODE);
        record.setScore(100);
        record.setSourceMode("all");
        Page<AppPracticeRecordItemVO> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(record));
        page.setTotal(1);
        when(practiceService.pagePracticeRecords(eq(USER_ID), any())).thenReturn(page);

        mockMvc.perform(get("/app/practice/records")
                        .requestAttr("userId", USER_ID)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].recordCode").value("PR-20260813-001"))
                .andExpect(jsonPath("$.data.records[0].chapterId").value(CHAPTER_CODE));
    }

    @Test
    @DisplayName("TC-M21-006 GET/POST/DELETE /app/notebook 错题本 CRUD（需 JWT）")
    void tcM21006_notebookCrud() throws Exception {
        AppNotebookItemVO item = new AppNotebookItemVO();
        item.setId(QUESTION_CODE);
        item.setTitle("下列各数中，属于负数的是（ ）");
        item.setSubject("数学 · 初三");
        when(notebookService.listNotebook(eq(USER_ID), any())).thenReturn(Collections.singletonList(item));

        AppNotebookAddResultVO addResult = new AppNotebookAddResultVO();
        addResult.setDuplicate(false);
        addResult.setItem(item);
        when(notebookService.addNotebook(eq(USER_ID), any(AppNotebookCreateBO.class))).thenReturn(addResult);
        doNothing().when(notebookService).removeNotebook(eq(USER_ID), eq(QUESTION_CODE));

        mockMvc.perform(get("/app/notebook").requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(QUESTION_CODE));

        AppNotebookCreateBO create = new AppNotebookCreateBO();
        create.setQuestionId(QUESTION_CODE);
        create.setSubject("数学 · 初三");
        mockMvc.perform(post("/app/notebook")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.duplicate").value(false));

        mockMvc.perform(delete("/app/notebook")
                        .requestAttr("userId", USER_ID)
                        .param("questionId", QUESTION_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("TC-M21-007 GET/POST/DELETE /app/collection 收藏 CRUD（需 JWT）")
    void tcM21007_collectionCrud() throws Exception {
        AppCollectionItemVO item = new AppCollectionItemVO();
        item.setId(QUESTION_CODE);
        item.setTitle("下列各数中，属于负数的是（ ）");
        item.setSubject("数学 · 初三");
        when(collectionService.listCollections(eq(USER_ID), any())).thenReturn(Collections.singletonList(item));

        AppCollectionToggleResultVO toggle = new AppCollectionToggleResultVO();
        toggle.setCollected(true);
        when(collectionService.addCollection(eq(USER_ID), any(AppCollectionCreateBO.class))).thenReturn(toggle);
        doNothing().when(collectionService).removeCollection(eq(USER_ID), eq(QUESTION_CODE));

        mockMvc.perform(get("/app/collection").requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].id").value(QUESTION_CODE));

        AppCollectionCreateBO create = new AppCollectionCreateBO();
        create.setQuestionId(QUESTION_CODE);
        create.setSubject("数学 · 初三");
        mockMvc.perform(post("/app/collection")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.collected").value(true));

        mockMvc.perform(delete("/app/collection")
                        .requestAttr("userId", USER_ID)
                        .param("questionId", QUESTION_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("TC-M21-008 POST /app/ocr/search OCR 搜题（需 JWT）")
    void tcM21008_ocrSearch() throws Exception {
        AppOcrSearchResultVO result = new AppOcrSearchResultVO();
        result.setRecognizedText("下列各数中，属于负数的是");
        when(ocrSearchService.search(eq(USER_ID), any(AppOcrSearchBO.class))).thenReturn(result);

        AppOcrSearchBO search = new AppOcrSearchBO();
        search.setText("下列各数中，属于负数的是");

        mockMvc.perform(post("/app/ocr/search")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(search)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.recognizedText").value("下列各数中，属于负数的是"));
    }

    @Test
    @DisplayName("TC-M21-009 POST /app/ai/chat AI 答疑（需 JWT）")
    void tcM21009_aiChat() throws Exception {
        AppAiChatResultVO result = new AppAiChatResultVO();
        result.setReply("移项变号是因为等式两边同时加减相同数，等式仍成立。");
        AppAiQuotaVO quota = new AppAiQuotaVO();
        quota.setAiDailyLimit(5);
        quota.setAiUsedToday(1);
        quota.setAiUnlimited(false);
        result.setQuota(quota);
        when(aiChatService.chat(eq(USER_ID), any(AppAiChatBO.class))).thenReturn(result);

        AppAiChatBO chat = new AppAiChatBO();
        chat.setMessage("为什么移项要变号？");

        mockMvc.perform(post("/app/ai/chat")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.reply").exists())
                .andExpect(jsonPath("$.data.quota.aiDailyLimit").value(5));
    }

    @Test
    @DisplayName("TC-M21-010 GET /app/analysis/overview 学情概览（需 JWT）")
    void tcM21010_analysisOverview() throws Exception {
        AppAnalysisOverviewVO overview = new AppAnalysisOverviewVO();
        overview.setWeekQuestionCount(42);
        overview.setCorrectRate(85);
        when(analysisOverviewService.getOverview(USER_ID)).thenReturn(overview);

        mockMvc.perform(get("/app/analysis/overview").requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.weekQuestionCount").value(42))
                .andExpect(jsonPath("$.data.correctRate").value(85));
    }

    @Test
    @DisplayName("TC-M21-011 GET /app/analysis/report 学情月报（需 JWT）")
    void tcM21011_analysisReport() throws Exception {
        AppAnalysisReportVO report = new AppAnalysisReportVO();
        report.setMonth("2026-08");
        report.setRankTitle("超过了 78% 的同龄人");
        when(analysisReportService.getReport(eq(USER_ID), any())).thenReturn(report);

        mockMvc.perform(get("/app/analysis/report")
                        .requestAttr("userId", USER_ID)
                        .param("month", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.month").value("2026-08"))
                .andExpect(jsonPath("$.data.rankTitle").exists());
    }

    @Test
    @DisplayName("TC-M21-012 GET/POST/DELETE /app/search/records 搜题记录（需 JWT）")
    void tcM21012_searchRecordsCrud() throws Exception {
        AppSearchRecordItemVO item = new AppSearchRecordItemVO();
        item.setTitle("一次函数图像性质");
        item.setSource("manual");
        item.setTime("刚刚");
        when(searchRecordService.listRecords(USER_ID)).thenReturn(Collections.singletonList(item));

        AppSearchRecordItemVO upserted = new AppSearchRecordItemVO();
        upserted.setTitle("已知二次函数 y=ax²");
        upserted.setSource("photo");
        when(searchRecordService.upsertRecord(eq(USER_ID), any(AppSearchRecordUpsertBO.class))).thenReturn(upserted);
        doNothing().when(searchRecordService).clearRecords(USER_ID);

        mockMvc.perform(get("/app/search/records").requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].title").value("一次函数图像性质"));

        AppSearchRecordUpsertBO upsert = new AppSearchRecordUpsertBO();
        upsert.setTitle("已知二次函数 y=ax²");
        upsert.setSource("photo");
        mockMvc.perform(post("/app/search/records")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upsert)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("已知二次函数 y=ax²"));

        mockMvc.perform(delete("/app/search/records").requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private AppQuestionListItemVO buildQuestionListItem() {
        AppQuestionListItemVO item = new AppQuestionListItemVO();
        item.setQuestionId(101L);
        item.setQuestionCode(QUESTION_CODE);
        item.setTitle("下列各数中，属于负数的是（ ）");
        item.setType("todo");
        item.setStatus("未做");
        return item;
    }
}
