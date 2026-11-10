package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.QuestionPaper;
import com.sqx.system.db.mapper.QuestionPaperMapper;
import com.sqx.system.db.service.IQuestionPaperService;
import org.springframework.stereotype.Service;

/**
 * 题库-题集 服务实现类
 *
 * @author sqx
 */
@Service
public class IQuestionPaperServiceImpl extends ServiceImpl<QuestionPaperMapper, QuestionPaper> implements IQuestionPaperService {
}
