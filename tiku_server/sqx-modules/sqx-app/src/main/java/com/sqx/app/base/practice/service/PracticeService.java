package com.sqx.app.base.practice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.practice.domain.bo.AppPracticeDraftQueryBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeDraftSaveBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeProgressQueryBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeRecordQueryBO;
import com.sqx.app.base.practice.domain.bo.AppPracticeSubmitBO;
import com.sqx.app.base.practice.domain.vo.AppPracticeDraftSaveResultVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeDraftVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeProgressVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeRecordItemVO;
import com.sqx.app.base.practice.domain.vo.AppPracticeSubmitResultVO;

/**
 * 用户端章节练习进度与提交
 */
public interface PracticeService {

    /**
     * 查询当前用户在指定章节的练习进度
     */
    AppPracticeProgressVO getChapterProgress(Long userId, AppPracticeProgressQueryBO query);

    /**
     * 提交练习答卷：服务端判分、持久化记录、更新错题/掌握
     */
    AppPracticeSubmitResultVO submitPractice(Long userId, AppPracticeSubmitBO submit);

    /**
     * 分页查询练习提交记录
     */
    IPage<AppPracticeRecordItemVO> pagePracticeRecords(Long userId, AppPracticeRecordQueryBO query);

    /**
     * 保存未交卷答题暂存（题号 + 已答明细）
     */
    AppPracticeDraftSaveResultVO savePracticeDraft(Long userId, AppPracticeDraftSaveBO save);

    /**
     * 恢复未交卷答题暂存
     */
    AppPracticeDraftVO getPracticeDraft(Long userId, AppPracticeDraftQueryBO query);
}
