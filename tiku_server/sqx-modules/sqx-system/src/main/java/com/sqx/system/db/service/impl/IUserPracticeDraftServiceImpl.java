package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.UserPracticeDraft;
import com.sqx.system.db.mapper.UserPracticeDraftMapper;
import com.sqx.system.db.service.IUserPracticeDraftService;
import org.springframework.stereotype.Service;

/**
 * 用户练习答题暂存 Service 实现
 *
 * @author sqx
 */
@Service
public class IUserPracticeDraftServiceImpl extends ServiceImpl<UserPracticeDraftMapper, UserPracticeDraft>
        implements IUserPracticeDraftService {
}
