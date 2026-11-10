package com.sqx.app.integration.m22;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqx.app.base.ai.controller.AppAiChatController;
import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.vo.AppAiChatResultVO;
import com.sqx.app.base.ai.domain.vo.AppAiQuotaVO;
import com.sqx.app.base.ai.enums.AiError;
import com.sqx.app.base.ai.factory.AiChatFactory;
import com.sqx.app.base.ai.service.AiChatService;
import com.sqx.app.base.ai.service.impl.AiChatServiceImpl;
import com.sqx.app.base.ai.service.impl.AiQuotaServiceImpl;
import com.sqx.app.base.ai.strategy.IAiChatPlatformStrategy;
import com.sqx.app.base.ai.support.AiConfigReader;
import com.sqx.app.base.ai.support.AiSensitiveWordFilter;
import com.sqx.app.base.ocr.controller.AppOcrSearchController;
import com.sqx.app.base.ocr.domain.bo.AppOcrSearchBO;
import com.sqx.app.base.ocr.domain.vo.AppOcrQuotaVO;
import com.sqx.app.base.ocr.domain.vo.AppOcrSearchResultVO;
import com.sqx.app.base.ocr.enums.OcrError;
import com.sqx.app.base.ocr.factory.OcrFactory;
import com.sqx.app.base.ocr.service.OcrSearchService;
import com.sqx.app.base.ocr.service.impl.OcrQuotaServiceImpl;
import com.sqx.app.base.ocr.service.impl.OcrSearchServiceImpl;
import com.sqx.app.base.ocr.support.OcrConfigReader;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.vip.expire.service.VipExpireService;
import com.sqx.app.vip.rules.support.MemberRulesSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.web.exception.GlobalExceptionHandler;
import com.sqx.system.db.service.IQuestionService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 模块二十二 P0#8 AI/OCR 次数双重校验：绕过 uniapp 本地校验，直连 POST 仍由服务端拦截/扣减
 */
@ExtendWith(MockitoExtension.class)
class M22QuotaCheckIntegrationTest {

    private static final Long USER_ID = 100L;
    private static final int DAILY_LIMIT = 5;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("MockMvc 直调 Controller（业务层 Mock）")
    class ControllerLayerTests {

        @Mock
        private AiChatService aiChatService;
        @Mock
        private OcrSearchService ocrSearchService;

        @InjectMocks
        private AppAiChatController aiChatController;
        @InjectMocks
        private AppOcrSearchController ocrSearchController;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();
            mockMvc = MockMvcBuilders.standaloneSetup(aiChatController, ocrSearchController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .setValidator(validator)
                    .build();
        }

        @Test
        @DisplayName("TC-M22-006 POST /app/ai/chat 次数用尽返回 1050010（绕过前端）")
        void tcM22006_aiChatQuotaExceeded() throws Exception {
            when(aiChatService.chat(any(Long.class), any(AppAiChatBO.class)))
                    .thenThrow(new BizException(AiError.QUOTA_EXCEEDED));

            AppAiChatBO chat = new AppAiChatBO();
            chat.setMessage("直连服务端提问");

            mockMvc.perform(post("/app/ai/chat")
                            .requestAttr("userId", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(chat)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(AiError.QUOTA_EXCEEDED.getCode()))
                    .andExpect(jsonPath("$.msg").value(AiError.QUOTA_EXCEEDED.getMessage()));
        }

        @Test
        @DisplayName("TC-M22-007 POST /app/ocr/search 次数用尽返回 1050008（绕过前端）")
        void tcM22007_ocrSearchQuotaExceeded() throws Exception {
            when(ocrSearchService.search(any(Long.class), any(AppOcrSearchBO.class)))
                    .thenThrow(new BizException(OcrError.QUOTA_EXCEEDED));

            AppOcrSearchBO search = new AppOcrSearchBO();
            search.setText("下列各数中，属于负数的是");

            mockMvc.perform(post("/app/ocr/search")
                            .requestAttr("userId", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(OcrError.QUOTA_EXCEEDED.getCode()))
                    .andExpect(jsonPath("$.msg").value(OcrError.QUOTA_EXCEEDED.getMessage()));
        }

        @Test
        @DisplayName("TC-M22-008 POST /app/ai/chat 成功返回 quota 快照供 uniapp 同步")
        void tcM22008_aiChatSuccessWithQuota() throws Exception {
            AppAiChatResultVO result = new AppAiChatResultVO();
            result.setReply("移项变号是因为等式两边同时加减相同数，等式仍成立。");
            AppAiQuotaVO quota = new AppAiQuotaVO();
            quota.setAiDailyLimit(DAILY_LIMIT);
            quota.setAiUsedToday(3);
            quota.setAiUnlimited(false);
            result.setQuota(quota);
            when(aiChatService.chat(any(Long.class), any(AppAiChatBO.class))).thenReturn(result);

            AppAiChatBO chat = new AppAiChatBO();
            chat.setMessage("为什么移项要变号？");

            mockMvc.perform(post("/app/ai/chat")
                            .requestAttr("userId", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(chat)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.quota.aiDailyLimit").value(DAILY_LIMIT))
                    .andExpect(jsonPath("$.data.quota.aiUsedToday").value(3))
                    .andExpect(jsonPath("$.data.quota.aiUnlimited").value(false));
        }

        @Test
        @DisplayName("TC-M22-009 POST /app/ocr/search 成功返回 quota 快照供 uniapp 同步")
        void tcM22009_ocrSearchSuccessWithQuota() throws Exception {
            AppOcrSearchResultVO result = new AppOcrSearchResultVO();
            result.setRecognizedText("下列各数中，属于负数的是");
            AppOcrQuotaVO quota = new AppOcrQuotaVO();
            quota.setOcrDailyLimit(DAILY_LIMIT);
            quota.setOcrUsedToday(2);
            quota.setOcrUnlimited(false);
            result.setQuota(quota);
            when(ocrSearchService.search(any(Long.class), any(AppOcrSearchBO.class))).thenReturn(result);

            AppOcrSearchBO search = new AppOcrSearchBO();
            search.setText("下列各数中，属于负数的是");

            mockMvc.perform(post("/app/ocr/search")
                            .requestAttr("userId", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.quota.ocrDailyLimit").value(DAILY_LIMIT))
                    .andExpect(jsonPath("$.data.quota.ocrUsedToday").value(2))
                    .andExpect(jsonPath("$.data.quota.ocrUnlimited").value(false));
        }
    }

    @Nested
    @DisplayName("真实配额服务链路（Controller → Service → QuotaService）")
    class RealQuotaServiceChainTests {

        @Mock
        private IUserService iUserService;
        @Mock
        private AiConfigReader aiConfigReader;
        @Mock
        private OcrConfigReader ocrConfigReader;
        @Mock
        private VipExpireService vipExpireService;
        @Mock
        private AiChatFactory aiChatFactory;
        @Mock
        private AiSensitiveWordFilter aiSensitiveWordFilter;
        @Mock
        private IAiChatPlatformStrategy aiPlatformStrategy;
        @Mock
        private IQuestionService iQuestionService;
        @Mock
        private OcrFactory ocrFactory;

        private AiQuotaServiceImpl aiQuotaService;
        private OcrQuotaServiceImpl ocrQuotaService;
        private MemberRulesSupport memberRulesSupport;
        private AiChatServiceImpl aiChatService;
        private OcrSearchServiceImpl ocrSearchService;
        private AppAiChatController aiChatController;
        private AppOcrSearchController ocrSearchController;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            Configuration configuration = new Configuration();
            MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
            TableInfoHelper.initTableInfo(assistant, UserEntity.class);

            aiQuotaService = new AiQuotaServiceImpl();
            memberRulesSupport = new MemberRulesSupport();
            injectField(aiQuotaService, "iUserService", iUserService);
            injectField(aiQuotaService, "aiConfigReader", aiConfigReader);
            injectField(aiQuotaService, "vipExpireService", vipExpireService);
            injectField(aiQuotaService, "memberRulesSupport", memberRulesSupport);

            ocrQuotaService = new OcrQuotaServiceImpl();
            injectField(ocrQuotaService, "iUserService", iUserService);
            injectField(ocrQuotaService, "ocrConfigReader", ocrConfigReader);
            injectField(ocrQuotaService, "vipExpireService", vipExpireService);
            injectField(ocrQuotaService, "memberRulesSupport", memberRulesSupport);

            aiChatService = new AiChatServiceImpl();
            injectField(aiChatService, "aiQuotaService", aiQuotaService);
            injectField(aiChatService, "aiChatFactory", aiChatFactory);
            injectField(aiChatService, "aiSensitiveWordFilter", aiSensitiveWordFilter);

            ocrSearchService = new OcrSearchServiceImpl();
            injectField(ocrSearchService, "iQuestionService", iQuestionService);
            injectField(ocrSearchService, "ocrConfigReader", ocrConfigReader);
            injectField(ocrSearchService, "ocrFactory", ocrFactory);
            injectField(ocrSearchService, "ocrQuotaService", ocrQuotaService);

            aiChatController = new AppAiChatController();
            injectField(aiChatController, "aiChatService", aiChatService);

            ocrSearchController = new AppOcrSearchController();
            injectField(ocrSearchController, "ocrSearchService", ocrSearchService);

            LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
            validator.afterPropertiesSet();
            mockMvc = MockMvcBuilders.standaloneSetup(aiChatController, ocrSearchController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .setValidator(validator)
                    .build();
        }

        private void injectField(Object target, String fieldName, Object value) {
            try {
                java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Failed to inject " + fieldName, ex);
            }
        }

        @Test
        @DisplayName("TC-M22-010 非会员 ai_used_today=5 直连 POST /app/ai/chat 返回 1050010 且不调用 LLM")
        void tcM22010_aiChatRealQuotaBlocksBeforeLlm() throws Exception {
            when(aiConfigReader.getDailyLimit()).thenReturn(DAILY_LIMIT);
            UserEntity exhausted = buildUser(USER_ID, 0, DAILY_LIMIT, LocalDate.now());
            when(vipExpireService.refreshIfExpired(USER_ID)).thenReturn(exhausted);
            doNothing().when(aiSensitiveWordFilter).validateChatInput(any(AppAiChatBO.class));

            AppAiChatBO chat = new AppAiChatBO();
            chat.setMessage("绕过前端本地校验直接请求");

            mockMvc.perform(post("/app/ai/chat")
                            .requestAttr("userId", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(chat)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(AiError.QUOTA_EXCEEDED.getCode()));

            verify(aiChatFactory, never()).getPlatform();
            verify(iUserService, never()).updateById(any(UserEntity.class));
        }

        @Test
        @DisplayName("TC-M22-011 非会员 ocr_used_today=5 直连 POST /app/ocr/search 返回 1050008 且不搜题")
        void tcM22011_ocrSearchRealQuotaBlocksBeforeMatch() throws Exception {
            when(ocrConfigReader.getDailyLimit()).thenReturn(DAILY_LIMIT);
            UserEntity exhausted = buildUser(USER_ID, 0, DAILY_LIMIT, LocalDate.now());
            exhausted.setOcrUsedToday(DAILY_LIMIT);
            exhausted.setOcrQuotaDate(LocalDate.now());
            when(vipExpireService.refreshIfExpired(USER_ID)).thenReturn(exhausted);

            AppOcrSearchBO search = new AppOcrSearchBO();
            search.setText("下列各数中，属于负数的是");

            mockMvc.perform(post("/app/ocr/search")
                            .requestAttr("userId", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(search)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(OcrError.QUOTA_EXCEEDED.getCode()));

            verify(iQuestionService, never()).list(any(Wrapper.class));
            verify(iUserService, never()).updateById(any(UserEntity.class));
        }

        @Test
        @DisplayName("TC-M22-012 非会员 ai_used_today=2 直连 POST /app/ai/chat 扣减至 3 并返回 quota")
        void tcM22012_aiChatRealQuotaDeductsAndReturnsSnapshot() throws Exception {
            when(aiConfigReader.getDailyLimit()).thenReturn(DAILY_LIMIT);
            UserEntity user = buildUser(USER_ID, 0, 2, LocalDate.now());
            when(vipExpireService.refreshIfExpired(USER_ID)).thenReturn(user);
            when(iUserService.updateById(any(UserEntity.class))).thenReturn(true);
            doNothing().when(aiSensitiveWordFilter).validateChatInput(any(AppAiChatBO.class));
            when(aiChatFactory.getPlatform()).thenReturn(aiPlatformStrategy);
            when(aiPlatformStrategy.chat(any(AppAiChatBO.class))).thenReturn("这是 AI 回复。");
            doNothing().when(aiSensitiveWordFilter).validateReply("这是 AI 回复。");

            AppAiChatBO chat = new AppAiChatBO();
            chat.setMessage("请讲解这道题");

            mockMvc.perform(post("/app/ai/chat")
                            .requestAttr("userId", USER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(chat)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.quota.aiUsedToday").value(3))
                    .andExpect(jsonPath("$.data.quota.aiDailyLimit").value(DAILY_LIMIT));

            verify(iUserService).updateById(any(UserEntity.class));
        }
    }

    private UserEntity buildUser(Long userId, int isVip, int aiUsedToday, LocalDate aiQuotaDate) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setIsVip(isVip);
        user.setAiUsedToday(aiUsedToday);
        user.setAiQuotaDate(aiQuotaDate);
        user.setOcrUsedToday(0);
        user.setOcrQuotaDate(aiQuotaDate);
        return user;
    }
}
