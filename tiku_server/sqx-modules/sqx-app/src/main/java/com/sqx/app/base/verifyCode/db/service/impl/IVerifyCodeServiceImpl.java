package com.sqx.app.base.verifyCode.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.verifyCode.db.service.IVerifyCodeService;
import com.sqx.app.base.verifyCode.domain.entity.VerifyCode;
import com.sqx.app.base.verifyCode.db.mapper.VerifyCodeMapper;
import org.springframework.stereotype.Service;

/**
 * 验证码 服务实现类
 *
 * @author sqx
 * @since 2025-04-22 15:31:50
 */
@Service
public class IVerifyCodeServiceImpl extends ServiceImpl<VerifyCodeMapper, VerifyCode> implements IVerifyCodeService {

}
