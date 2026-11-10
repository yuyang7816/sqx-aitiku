package com.sqx.system.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.system.db.entity.Banner;
import com.sqx.system.db.mapper.BannerMapper;
import com.sqx.system.db.service.IBannerService;
import org.springframework.stereotype.Service;

/**
 * banner 服务实现类
 *
 * @author sqx
 * @since 2025-05-16 13:49:04
 */
@Service
public class IBannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements IBannerService {
}
