package com.sqx.system.db.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.common.core.exception.SqxException;
import com.sqx.common.redis.utils.RedisKeys;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.mapper.SysConfigDao;
import com.sqx.system.db.service.ISysConfigService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ISysConfigServiceImpl extends ServiceImpl<SysConfigDao, SysConfigEntity> implements ISysConfigService {

    @Override
    public String getConfigValue(String configKey) {
        String key = RedisKeys.SYS_CONFIG_KEY + configKey;
        if (RedisUtil.hasKey(key)) {
            String cacheSysConfig = RedisUtil.getCacheObject(key);
            SysConfigEntity sysConfig = JSONObject.parseObject(cacheSysConfig, SysConfigEntity.class);
            return sysConfig.getParamValue();
        }

        SysConfigEntity sysConfig = this.getOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, configKey)
        );
        if (Objects.isNull(sysConfig)) {
            throw new SqxException("系统配置不存在");
        }

        return sysConfig.getParamValue();
    }

    @Override
    public void loadSysConfig() {
        List<SysConfigEntity> sysConfigList = this.list();
        if (CollectionUtils.isNotEmpty(sysConfigList)) {
            sysConfigList.parallelStream()
                    .forEach(item -> RedisUtil.setCacheObject(RedisKeys.SYS_CONFIG_KEY + item.getParamKey(), JSONObject.toJSONString(item)));
        }
    }
}
