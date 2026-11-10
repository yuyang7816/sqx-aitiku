package com.sqx.app.base.notebook.service;

import com.sqx.app.base.notebook.domain.bo.AppNotebookCreateBO;
import com.sqx.app.base.notebook.domain.bo.AppNotebookQueryBO;
import com.sqx.app.base.notebook.domain.bo.AppNotebookUpdateBO;
import com.sqx.app.base.notebook.domain.vo.AppNotebookAddResultVO;
import com.sqx.app.base.notebook.domain.vo.AppNotebookItemVO;

import java.util.List;

/**
 * 用户端错题本云端同步
 */
public interface NotebookService {

    List<AppNotebookItemVO> listNotebook(Long userId, AppNotebookQueryBO query);

    AppNotebookAddResultVO addNotebook(Long userId, AppNotebookCreateBO create);

    void removeNotebook(Long userId, String questionId);

    void clearNotebook(Long userId);

    AppNotebookItemVO updateMastered(Long userId, AppNotebookUpdateBO update);
}
