package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.Question;
import com.sqx.system.db.mapper.QuestionMapper;
import com.sqx.system.db.service.IQuestionService;
import org.springframework.stereotype.Service;

/**
 * 题库-题目 服务实现类
 *
 * @author sqx
 */
@Service
public class IQuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {
}
