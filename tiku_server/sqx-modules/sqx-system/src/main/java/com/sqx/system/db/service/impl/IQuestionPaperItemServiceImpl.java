package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.QuestionPaperItem;
import com.sqx.system.db.mapper.QuestionPaperItemMapper;
import com.sqx.system.db.service.IQuestionPaperItemService;
import org.springframework.stereotype.Service;

/**
 * 题库-题集题目关联 服务实现类
 *
 * @author sqx
 */
@Service
public class IQuestionPaperItemServiceImpl extends ServiceImpl<QuestionPaperItemMapper, QuestionPaperItem> implements IQuestionPaperItemService {
}
