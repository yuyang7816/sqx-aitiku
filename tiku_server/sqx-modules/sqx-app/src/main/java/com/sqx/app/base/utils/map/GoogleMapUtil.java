package com.sqx.app.base.utils.map;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/5/13
 * 谷歌地图工具类
 */
@Slf4j
public class GoogleMapUtil {

    public static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    /**
     * 根据坐标查询地址
     * @param lat 纬度
     * @param lng 经度
     * @param language 返回语言
     * @return
     */
    public static AddressResult getAddressByCoor(String lat, String lng, String language) {
        Map<String, String> addressMap = new HashMap<>();

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("latlng", lat + "," + lng);
        paramMap.put("key", getKey());
        paramMap.put("language", language);
        String result = OkHttpUtil.doGet(GEOCODE_URL, paramMap);
        //String result = HttpClientUtil.doGet(GEOCODE_URL, paramMap);
        if (StringUtils.isBlank(result)) {
            throw new BizException("谷歌地图请求失败");
        }

        JSONObject resultJson = JSONObject.parseObject(result);
        log.info("谷歌地图返回值: {}", resultJson);

        String status = resultJson.getString("status");
        if (!Objects.equals("OK", status)) {
            throw new BizException("谷歌地图请求失败");
        }

        JSONArray results = resultJson.getJSONArray("results");
        JSONObject addressJson = results.getObject(0, JSONObject.class);
        JSONArray addressComponents = addressJson.getJSONArray("address_components");

        // 详细地址
        for (Object addressComponent : addressComponents) {
            JSONObject addressComponentJson = JSONObject.parseObject(addressComponent.toString());
            List<String> types = addressComponentJson.getJSONArray("types").toJavaList(String.class);

            // 一级区划
            if (types.contains("country")) {
                String firstAddress = addressComponentJson.getString("long_name");
                putOrDefault(addressMap, "1", firstAddress);
            }

            // 二级区划
            if (types.contains("administrative_area_level_1")) {
                String secondAddress = addressComponentJson.getString("long_name");
                putOrDefault(addressMap, "2", secondAddress);
            }

            // 三级区划
            if (types.contains("administrative_area_level_2")) {
                String thirdAddress = addressComponentJson.getString("long_name");
                putOrDefault(addressMap, "3", thirdAddress);
            }

            // 四级区划
            if (types.contains("locality")) {
                String fourthAddress = addressComponentJson.getString("long_name");
                putOrDefault(addressMap, "4", fourthAddress);
            }

            // 五级区划
            if (types.contains("sublocality_level_1")) {
                String fiveAddress = addressComponentJson.getString("long_name");
                putOrDefault(addressMap, "5", fiveAddress);
            }

            // 六级区划
            if (types.contains("route")) {
                String sixthAddress = addressComponentJson.getString("long_name");
                putOrDefault(addressMap, "6", sixthAddress);
            }

            // 七级地址
            if (types.contains("street_number")) {
                String sevenAddress = addressComponentJson.getString("long_name");
                putOrDefault(addressMap, "7", sevenAddress);
            }
        }

        // 格式化地址
        String formattedAddress = addressJson.getString("formatted_address");

        // 谷歌地图的地址id
        String placeId = addressJson.getString("place_id");

        AddressResult addressResult = new AddressResult();
        addressResult.setAddressMap(addressMap);
        addressResult.setPlaceId(placeId);
        addressResult.setFormattedAddress(formattedAddress);

        return addressResult;
    }

    /**
     * 获取谷歌地图key
     * @return
     */
    public static String getKey() {
        return SysConfigUtil.getConfigValue("google_map_key");
    }

    private static void putOrDefault(Map<String, String> map, String key, String value) {
        map.put(key, value != null ? value : "");
    }
}
