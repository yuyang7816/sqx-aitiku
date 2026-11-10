package com.sqx.app.base.map.strategy;

import com.alibaba.fastjson.JSONObject;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/8/11
 * <p>
 * 天地图
 */
@Slf4j
public class TianDiTuMapPlatformStrategy implements IMapPlatformStrategy {

    @Override
    public AddressVo reverseGeocode(String lat, String lng) {
        String api = "http://api.tianditu.gov.cn/geocoder";
        String key = SysConfigUtil.getConfigValue("map_tianditu_key");

        Map<String, String> param = new HashMap<>();
        JSONObject postStr = new JSONObject();
        postStr.put("lon", lng);
        postStr.put("lat", lat);
        postStr.put("ver", "1");
        param.put("postStr", postStr.toJSONString());
        param.put("type", "geocode");
        param.put("tk", key);

        log.info(param.toString());
        String resp = OkHttpUtil.doGet(api, param);
        JSONObject respJson = JSONObject.parseObject(resp);
        log.info("{}", respJson);
        String status = respJson.getString("status");
        if (!Objects.equals("0", status)) {
            throw new BizException("请求天地图失败");
        }

        JSONObject result = respJson.getJSONObject("result");
        JSONObject addressComponent = result.getJSONObject("addressComponent");

        String nation = addressComponent.getString("nation");
        String province = addressComponent.getString("province");
        String city = addressComponent.getString("city");
        String county = addressComponent.getString("county");
        String town = addressComponent.getString("town");
        String road = addressComponent.getString("road");
        String poi = addressComponent.getString("poi");

        AddressVo vo = new AddressVo();
        vo.setCountry(nation);
        vo.setProvince(province);
        vo.setCity(city);
        vo.setDistrict(county);
        vo.setTown(town);
        vo.setRoad(road);
        vo.setAddress(poi);

        return vo;
    }
}
