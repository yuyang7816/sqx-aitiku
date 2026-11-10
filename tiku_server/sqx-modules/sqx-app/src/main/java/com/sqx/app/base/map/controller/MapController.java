package com.sqx.app.base.map.controller;

import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.app.base.map.service.MapService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chengming
 * @since 2025/7/22
 */
@Tag(name = "地图")
@RestController
@RequestMapping("app/map")
public class MapController {

    @Resource
    private MapService mapService;

    @Operation(summary = "反向地理编码（根据经纬度获取地址信息）")
    @GetMapping("reverseGeocode")
    public Result<AddressVo> reverseGeocode(String lat, String lng) {
        AddressVo vo = mapService.reverseGeocode(lat, lng);
        return Result.success(vo);
    }
}
