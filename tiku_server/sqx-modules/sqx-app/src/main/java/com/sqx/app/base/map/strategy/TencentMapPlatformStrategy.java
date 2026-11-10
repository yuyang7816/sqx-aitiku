package com.sqx.app.base.map.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/8/11
 * <p>
 * 腾讯地图
 */
@Slf4j
public class TencentMapPlatformStrategy implements IMapPlatformStrategy {

    @Override
    public AddressVo reverseGeocode(String lat, String lng) {
        if (StringUtils.isBlank(lat) || StringUtils.isBlank(lng)) {
            throw new IllegalArgumentException("经纬度不能为空");
        }

        String api = "https://apis.map.qq.com/ws/geocoder/v1/";
        String key = SysConfigUtil.getConfigValue("map_tencent_key");

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("location", lat + "," + lng);
        paramMap.put("key", key);

        String resp;
        try {
            resp = OkHttpUtil.doGet(api, paramMap);
            if (StringUtils.isBlank(resp)) {
                throw new BizException("请求腾讯地图失败: empty response");
            }
        } catch (Exception e) {
            throw new BizException("请求腾讯地图失败: " + e.getMessage(), e);
        }

        JSONObject respJson = JSON.parseObject(resp);
        Integer status = respJson.getInteger("status");
        if (!Objects.equals(0, status)) {
            throw new BizException("腾讯地图解析错误: " + respJson.getString("message"));
        }

        JSONObject result = respJson.getJSONObject("result");
        JSONObject addressComponent = result.getJSONObject("address_component");

        String nation = addressComponent.getString("nation");
        String province = addressComponent.getString("province");
        String city = addressComponent.getString("city");
        String district = addressComponent.getString("district");
        String street = addressComponent.getString("street");
        String streetNumber = addressComponent.getString("street_number");

        AddressVo vo = new AddressVo();
        vo.setCountry(nation);
        vo.setProvince(province);
        vo.setCity(city);
        vo.setDistrict(district);
        vo.setTown(street);
        vo.setRoad(streetNumber);

        return vo;
    }
}
