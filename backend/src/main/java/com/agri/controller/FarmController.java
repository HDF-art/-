package com.agri.controller;

import com.agri.model.Farm;
import com.agri.service.FarmService;
import com.agri.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 农场控制器
 */
@RestController
@RequestMapping("/farms")
public class FarmController {

    @Autowired
    private FarmService farmService;

    /**
     * 获取农场列表
     */
    @GetMapping("/list")
    public ResponseUtils.ApiResponse<List<Map<String, Object>>> getFarmList() {
        try {
            List<Farm> farms = farmService.list();
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (Farm farm : farms) {
                Map<String, Object> farmMap = new HashMap<>();
                farmMap.put("id", farm.getId());
                farmMap.put("farmName", farm.getName());
                farmMap.put("ownerName", farm.getAdminId() != null ? "管理员" + farm.getAdminId() : "未分配");
                farmMap.put("location", farm.getLocation());
                farmMap.put("area", farm.getArea());
                farmMap.put("cropType", farm.getMainCrops());
                farmMap.put("createTime", farm.getCreatedAt());
                result.add(farmMap);
            }
            
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取农场列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取农场信息
     * @param farmId 农场ID
     * @return 农场信息
     */
    @GetMapping("/{id}")
    public Farm getFarmById(@PathVariable("id") Long farmId) {
        return farmService.getById(farmId);
    }

    /**
     * 根据管理员ID获取农场信息
     * @param adminId 管理员ID
     * @return 农场信息
     */
    @GetMapping("/admin/{adminId}")
    public Farm getFarmByAdminId(@PathVariable Long adminId) {
        return farmService.findByAdminId(adminId);
    }

    /**
     * 创建农场
     * @param farm 农场信息
     * @return 创建结果
     */
    @PostMapping
    public boolean createFarm(@RequestBody Farm farm) {
        return farmService.createFarm(farm);
    }

    /**
     * 更新农场信息
     * @param farm 农场信息
     * @return 更新结果
     */
    @PutMapping
    public boolean updateFarm(@RequestBody Farm farm) {
        return farmService.updateFarm(farm);
    }

    /**
     * 删除农场
     * @param farmId 农场ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public boolean deleteFarm(@PathVariable("id") Long farmId) {
        return farmService.removeById(farmId);
    }

}