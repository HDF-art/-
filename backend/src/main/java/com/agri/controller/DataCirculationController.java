package com.agri.controller;

import com.agri.model.DataCirculation;
import com.agri.service.DataCirculationService;
import com.agri.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 数据流通控制器
 */
@RestController
@RequestMapping("/data-circulation")
public class DataCirculationController {

    @Autowired
    private DataCirculationService dataCirculationService;

    /**
     * 发布数据集交易信息
     * @param dataCirculation 数据流通信息
     * @return 发布结果
     */
    @PostMapping("/publish")
    public ResponseUtils.ApiResponse<DataCirculation> publishData(@RequestBody DataCirculation dataCirculation) {
        try {
            boolean result = dataCirculationService.publishData(dataCirculation);
            if (result) {
                return ResponseUtils.success(dataCirculation);
            } else {
                return ResponseUtils.error(500, "发布失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "发布失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有待交易的数据集
     * @return 数据集列表
     */
    @GetMapping("/available")
    public ResponseUtils.ApiResponse<List<DataCirculation>> getAvailableData() {
        try {
            List<DataCirculation> dataList = dataCirculationService.getAvailableData();
            return ResponseUtils.success(dataList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "获取数据集列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据发布者ID获取数据集列表
     * @param publisherId 发布者ID
     * @return 数据集列表
     */
    @GetMapping("/publisher/{publisherId}")
    public ResponseUtils.ApiResponse<List<DataCirculation>> getByPublisherId(@PathVariable("publisherId") Long publisherId) {
        try {
            List<DataCirculation> dataList = dataCirculationService.getByPublisherId(publisherId);
            return ResponseUtils.success(dataList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "获取数据集列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取数据流通信息
     * @param id 数据流通ID
     * @return 数据流通信息
     */
    @GetMapping("/{id}")
    public ResponseUtils.ApiResponse<DataCirculation> getById(@PathVariable("id") Long id) {
        try {
            DataCirculation dataCirculation = dataCirculationService.getById(id);
            return ResponseUtils.success(dataCirculation);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "获取数据流通信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新数据流通状态
     * @param id 数据流通ID
     * @param status 状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public ResponseUtils.ApiResponse<Boolean> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        try {
            boolean result = dataCirculationService.updateStatus(id, status);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "更新状态失败: " + e.getMessage());
        }
    }

}
