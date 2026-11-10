package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.QuestionChapter;
import com.sqx.system.db.mapper.QuestionChapterMapper;
import com.sqx.system.db.service.IQuestionChapterService;
import org.springframework.stereotype.Service;

/**
 * 题库-章 服务实现类
 *
 * @author sqx
 */
@Service
public class IQuestionChapterServiceImpl extends ServiceImpl<QuestionChapterMapper, QuestionChapter> implements IQuestionChapterService {
}
