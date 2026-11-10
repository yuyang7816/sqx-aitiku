package com.sqx.app.base.map.service;


import com.sqx.app.base.map.domain.vo.AddressVo;

/**
 * @author chengming
 * @since 2025/7/22
 */
public interface MapService {
    AddressVo reverseGeocode(String lat, String lng);
}
