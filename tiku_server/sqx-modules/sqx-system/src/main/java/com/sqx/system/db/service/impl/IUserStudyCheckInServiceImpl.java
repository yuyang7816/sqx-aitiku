package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.UserStudyCheckIn;
import com.sqx.system.db.mapper.UserStudyCheckInMapper;
import com.sqx.system.db.service.IUserStudyCheckInService;
import org.springframework.stereotype.Service;

/**
 * 用户学习日历打卡 服务实现
 */
@Service
public class IUserStudyCheckInServiceImpl extends ServiceImpl<UserStudyCheckInMapper, UserStudyCheckIn>
        implements IUserStudyCheckInService {
}
