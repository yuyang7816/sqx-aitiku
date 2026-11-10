package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.UserPracticeRecord;
import com.sqx.system.db.mapper.UserPracticeRecordMapper;
import com.sqx.system.db.service.IUserPracticeRecordService;
import org.springframework.stereotype.Service;

/**
 * 用户练习提交记录 Service 实现
 *
 * @author sqx
 */
@Service
public class IUserPracticeRecordServiceImpl extends ServiceImpl<UserPracticeRecordMapper, UserPracticeRecord>
        implements IUserPracticeRecordService {
}
