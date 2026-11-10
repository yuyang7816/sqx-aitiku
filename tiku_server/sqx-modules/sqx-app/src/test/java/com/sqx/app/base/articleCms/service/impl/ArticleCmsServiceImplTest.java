package com.sqx.app.base.articleCms.service.impl;

import com.sqx.app.base.articleCms.domain.vo.ArticleCmsVO;
import com.sqx.app.base.articleCms.support.ArticleConfigValueReader;
import com.sqx.app.base.enums.AppError;
import com.sqx.common.core.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 协议与政策 CMS API 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ArticleCmsServiceImplTest {

    @Mock
    private ArticleConfigValueReader articleConfigValueReader;

    @InjectMocks
    private ArticleCmsServiceImpl articleCmsService;

    @Test
    void getByType_whenUnknownType_shouldThrow() {
        BizException ex = assertThrows(BizException.class, () -> articleCmsService.getByType("invalid"));
        assertEquals(AppError.INFO_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getByType_whenAgreementFromConfig_shouldReturnStructuredContent() {
        when(articleConfigValueReader.getValueOrDefault("app_article_agreement_title")).thenReturn("用户协议 v2");
        when(articleConfigValueReader.getValueOrDefault("app_article_agreement_sub")).thenReturn("请阅读协议");
        when(articleConfigValueReader.getValueOrDefault("app_article_agreement_content"))
                .thenReturn("<h3>账号使用</h3><p>自定义正文</p>");

        ArticleCmsVO vo = articleCmsService.getByType("agreement");

        assertEquals("agreement", vo.getType());
        assertEquals("用户协议 v2", vo.getTitle());
        assertEquals("请阅读协议", vo.getSub());
        assertEquals("<h3>账号使用</h3><p>自定义正文</p>", vo.getContent());
    }

    @Test
    void getByType_whenPrivacyConfigMissing_shouldFallbackToDefaults() {
        when(articleConfigValueReader.getValueOrDefault("app_article_privacy_title")).thenReturn(null);
        when(articleConfigValueReader.getValueOrDefault("app_article_privacy_sub")).thenReturn("");
        when(articleConfigValueReader.getValueOrDefault("app_article_privacy_content")).thenReturn("   ");

        ArticleCmsVO vo = articleCmsService.getByType("privacy");

        assertEquals("privacy", vo.getType());
        assertEquals("隐私政策", vo.getTitle());
        assertEquals("我们重视你的个人信息安全，并尽量以最小化方式处理必要数据。", vo.getSub());
        assertTrue(vo.getContent().contains("信息收集"));
    }

    @Test
    void getByType_whenAbout_shouldReturnDefaultsWhenConfigBlank() {
        when(articleConfigValueReader.getValueOrDefault("app_article_about_title")).thenReturn(null);
        when(articleConfigValueReader.getValueOrDefault("app_article_about_sub")).thenReturn(null);
        when(articleConfigValueReader.getValueOrDefault("app_article_about_content")).thenReturn(null);

        ArticleCmsVO vo = articleCmsService.getByType("about");

        assertEquals("about", vo.getType());
        assertEquals("关于我们", vo.getTitle());
        assertTrue(vo.getContent().contains("产品理念"));
    }
}
