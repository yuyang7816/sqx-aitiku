package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.KnowledgePointQuestion;
import com.sqx.system.db.mapper.KnowledgePointQuestionMapper;
import com.sqx.system.db.service.IKnowledgePointQuestionService;
import org.springframework.stereotype.Service;

/**
 * 知识点-题目关联服务实现
 */
@Service
public class IKnowledgePointQuestionServiceImpl extends ServiceImpl<KnowledgePointQuestionMapper, KnowledgePointQuestion>
        implements IKnowledgePointQuestionService {
}
