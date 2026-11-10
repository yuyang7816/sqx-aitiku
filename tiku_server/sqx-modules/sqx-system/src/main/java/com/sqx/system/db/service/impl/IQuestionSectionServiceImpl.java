package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.QuestionSection;
import com.sqx.system.db.mapper.QuestionSectionMapper;
import com.sqx.system.db.service.IQuestionSectionService;
import org.springframework.stereotype.Service;

/**
 * 题库-节 服务实现类
 *
 * @author sqx
 */
@Service
public class IQuestionSectionServiceImpl extends ServiceImpl<QuestionSectionMapper, QuestionSection> implements IQuestionSectionService {
}
