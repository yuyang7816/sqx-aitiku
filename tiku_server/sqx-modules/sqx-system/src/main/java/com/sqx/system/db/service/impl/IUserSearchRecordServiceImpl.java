package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.UserSearchRecord;
import com.sqx.system.db.mapper.UserSearchRecordMapper;
import com.sqx.system.db.service.IUserSearchRecordService;
import org.springframework.stereotype.Service;

/**
 * 用户搜题记录 服务实现
 */
@Service
public class IUserSearchRecordServiceImpl extends ServiceImpl<UserSearchRecordMapper, UserSearchRecord>
        implements IUserSearchRecordService {
}
