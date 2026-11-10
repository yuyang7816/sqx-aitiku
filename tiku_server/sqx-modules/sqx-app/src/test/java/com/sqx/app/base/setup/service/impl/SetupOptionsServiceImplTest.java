package com.sqx.app.base.setup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.setup.constants.SetupDictConstant;
import com.sqx.app.base.setup.domain.vo.SetupGradeOptionVO;
import com.sqx.app.base.setup.domain.vo.SetupOptionsVO;
import com.sqx.app.base.setup.domain.vo.SetupSubjectOptionVO;
import com.sqx.app.base.setup.domain.vo.SetupVersionOptionVO;
import com.sqx.app.base.setup.support.SetupOptionsDefaults;
import com.sqx.system.db.entity.SysDictEntity;
import com.sqx.system.db.service.ISysDictService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/setup/options 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SetupOptionsServiceImplTest {

    @Mock
    private ISysDictService sysDictService;

    @InjectMocks
    private SetupOptionsServiceImpl setupOptionsService;

    @Test
    void getSetupOptions_whenDictEmpty_shouldReturnDefaultsMatchingPlanMock() {
        when(sysDictService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        SetupOptionsVO vo = setupOptionsService.getSetupOptions();

        assertEquals(SetupOptionsDefaults.defaultGradeStages().size(), vo.getGradeStages().size());
        assertEquals("小学", vo.getGradeStages().get(0).getLabel());
        assertEquals("primary", vo.getGradeStages().get(0).getValue());

        assertEquals(SetupOptionsDefaults.defaultGrades().size(), vo.getGrades().size());
        assertEquals("初一", vo.getGrades().get(6).getLabel());
        assertEquals("初中一年级", vo.getGrades().get(6).getValue());
        assertEquals("junior", vo.getGrades().get(6).getStageCode());

        assertEquals(6, vo.getSubjects().size());
        SetupSubjectOptionVO math = vo.getSubjects().get(0);
        assertEquals("数学", math.getName());
        assertEquals("file-text", math.getIcon());
        assertEquals(Arrays.asList("primary", "junior", "senior"), math.getStageCodes());
        assertFalse(math.getActive());

        SetupSubjectOptionVO chemistry = vo.getSubjects().stream()
                .filter(item -> "化学".equals(item.getName()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        assertEquals(Arrays.asList("junior", "senior"), chemistry.getStageCodes());

        assertEquals(3, vo.getVersions().size());
        assertEquals("人教版", vo.getVersions().get(0).getLabel());
        assertEquals("人教版", vo.getVersions().get(0).getValue());
    }

    @Test
    void getSetupOptions_whenDictPresent_shouldMapFromSysDict() {
        when(sysDictService.list(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(
                dict(1L, SetupDictConstant.TYPE_GRADE, "初中四年级", "初四", "初中四年级", 2, "junior", 1),
                dict(2L, SetupDictConstant.TYPE_GRADE, "初中五年级", "初五", "初中五年级", 1, "junior", 1),
                dict(3L, SetupDictConstant.TYPE_SUBJECT, "历史", "clock", "历史", 1, null, 1),
                dict(4L, SetupDictConstant.TYPE_VERSION, "沪教版", "hujiao", "沪教版", 1, null, 1)
        ));

        SetupOptionsVO vo = setupOptionsService.getSetupOptions();

        List<SetupGradeOptionVO> grades = vo.getGrades();
        assertEquals(2, grades.size());
        assertEquals("初五", grades.get(0).getLabel());
        assertEquals("初中五年级", grades.get(0).getValue());
        assertEquals("junior", grades.get(0).getStageCode());
        assertEquals("初四", grades.get(1).getLabel());

        SetupSubjectOptionVO subject = vo.getSubjects().get(0);
        assertEquals("历史", subject.getName());
        assertEquals("clock", subject.getIcon());
        assertEquals(Arrays.asList("junior", "senior"), subject.getStageCodes());
        assertFalse(subject.getActive());

        SetupVersionOptionVO version = vo.getVersions().get(0);
        assertEquals("沪教版", version.getLabel());
        assertEquals("沪教版", version.getValue());
    }

    @Test
    void getSetupOptions_shouldQueryAllSetupDictTypesInOneBatch() {
        when(sysDictService.list(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        setupOptionsService.getSetupOptions();

        ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(sysDictService).list(captor.capture());
    }

    private SysDictEntity dict(Long id, String type, String name, String code, String value, Integer orderNum, String remark, Integer status) {
        SysDictEntity entity = new SysDictEntity();
        entity.setId(id);
        entity.setType(type);
        entity.setName(name);
        entity.setCode(code);
        entity.setValue(value);
        entity.setOrderNum(orderNum);
        entity.setRemark(remark);
        entity.setStatus(status);
        return entity;
    }
}
