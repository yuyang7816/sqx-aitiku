package com.sqx.app.base.user.delete.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.user.delete.db.mapper.UserAccountDeletionRequestMapper;
import com.sqx.app.base.user.delete.db.service.IUserAccountDeletionRequestService;
import com.sqx.app.base.user.delete.domain.entity.UserAccountDeletionRequest;
import org.springframework.stereotype.Service;

@Service
public class IUserAccountDeletionRequestServiceImpl
        extends ServiceImpl<UserAccountDeletionRequestMapper, UserAccountDeletionRequest>
        implements IUserAccountDeletionRequestService {
}
