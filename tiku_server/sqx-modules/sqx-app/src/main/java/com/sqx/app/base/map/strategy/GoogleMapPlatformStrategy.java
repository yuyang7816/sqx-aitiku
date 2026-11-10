package com.sqx.app.base.map.strategy;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/8/11
 * <p>
 * 谷歌地图
 */
public class GoogleMapPlatformStrategy implements IMapPlatformStrategy {

    @Override
    public AddressVo reverseGeocode(String lat, String lng) {
        String api = "https://maps.googleapis.com/maps/api/geocode/json";
        String key = SysConfigUtil.getConfigValue("google_map_key");

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("latlng", lat + "," + lng);
        paramMap.put("key", key);
        paramMap.put("language", "zh"); // 默认中文

        String resp = OkHttpUtil.doGet(api, paramMap);
        if (StringUtils.isBlank(resp)) {
            throw new BizException("谷歌地图请求失败");
        }

        JSONObject respJson = JSONObject.parseObject(resp);
        String status = respJson.getString("status");
        if (!Objects.equals("OK", status)) {
            throw new BizException("谷歌地图请求失败");
        }

        JSONArray results = respJson.getJSONArray("results");
        JSONObject addressJson = results.getObject(0, JSONObject.class);
        JSONArray addressComponents = addressJson.getJSONArray("address_components");

        AddressVo vo = new AddressVo();
        for (Object addressComponent : addressComponents) {
            JSONObject addressComponentJson = JSONObject.parseObject(addressComponent.toString());
            List<String> types = addressComponentJson.getJSONArray("types").toJavaList(String.class);

            // 国家
            if (types.contains("country")) {
                String country = addressComponentJson.getString("long_name");
                vo.setCountry(country);
            }

            // 省
            if (types.contains("administrative_area_level_1")) {
                String province = addressComponentJson.getString("long_name");
                vo.setProvince(province);
            }

            // 市
            if (types.contains("administrative_area_level_2")) {
                String city = addressComponentJson.getString("long_name");
                vo.setCity(city);
            }

            // 区
            if (types.contains("locality")) {
                String district = addressComponentJson.getString("long_name");
                vo.setDistrict(district);
            }

            // 街道/城镇
            if (types.contains("sublocality_level_1")) {
                String town = addressComponentJson.getString("long_name");
                vo.setTown(town);
            }

            // 路/街
            if (types.contains("route")) {
                String road = addressComponentJson.getString("long_name");
                vo.setRoad(road);
            }

            // 地址
            if (types.contains("street_number")) {
                String address = addressComponentJson.getString("long_name");
                vo.setAddress(address);
            }
        }

        return vo;
    }
}
