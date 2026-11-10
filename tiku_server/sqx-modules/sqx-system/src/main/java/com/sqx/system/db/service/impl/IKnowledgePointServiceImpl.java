package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.KnowledgePoint;
import com.sqx.system.db.mapper.KnowledgePointMapper;
import com.sqx.system.db.service.IKnowledgePointService;
import org.springframework.stereotype.Service;

/**
 * 知识点服务实现
 */
@Service
public class IKnowledgePointServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint>
        implements IKnowledgePointService {
}
