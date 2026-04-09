package com.agri.controller;

import com.agri.model.Dataset;
import com.agri.service.DatasetService;
import com.agri.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 数据集控制器
 */
@RestController
@RequestMapping("/datasets")
public class DatasetController {

    @Autowired
    private DatasetService datasetService;

    @Value("${file.upload-dir:D:/agri-platform/upload/}")
    private String uploadDirBase;

    /**
     * 上传数据集
     * @param file 数据集文件
     * @param name 数据集名称
     * @param type 数据集类型
     * @param description 描述
     * @param uploaderId 上传者ID
     * @param farmId 农场ID
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ResponseUtils.ApiResponse<Dataset> uploadDataset(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("description") String description,
            @RequestParam("uploaderId") Long uploaderId,
            @RequestParam("farmId") Long farmId) {
        try {
            // 确保上传目录存在
            String datasetDir = uploadDirBase + "datasets/" + farmId + "/";
            File dir = new File(datasetDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            String filePath = datasetDir + fileName;

            // 保存文件
            file.transferTo(new File(filePath));

            // 创建数据集记录
            Dataset dataset = new Dataset();
            dataset.setName(name);
            dataset.setPath(filePath);
            dataset.setSize(file.getSize());
            dataset.setType(type);
            dataset.setDescription(description);
            dataset.setUploaderId(uploaderId);
            dataset.setFarmId(farmId);

            boolean result = datasetService.uploadDataset(dataset);
            if (result) {
                return ResponseUtils.success(dataset);
            } else {
                return ResponseUtils.error(500, "上传失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据集列表
     * @return 数据集列表
     */
    @GetMapping
    public ResponseUtils.ApiResponse<List<Dataset>> getDatasetList() {
        try {
            List<Dataset> datasets = datasetService.list();
            return ResponseUtils.success(datasets);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取数据集列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据上传者ID获取数据集列表
     * @param uploaderId 上传者ID
     * @return 数据集列表
     */
    @GetMapping("/uploader/{uploaderId}")
    public ResponseUtils.ApiResponse<List<Dataset>> getDatasetsByUploaderId(@PathVariable("uploaderId") Long uploaderId) {
        try {
            List<Dataset> datasets = datasetService.getByUploaderId(uploaderId);
            return ResponseUtils.success(datasets);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取数据集列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据农场ID获取数据集列表
     * @param farmId 农场ID
     * @return 数据集列表
     */
    @GetMapping("/farm/{farmId}")
    public ResponseUtils.ApiResponse<List<Dataset>> getDatasetsByFarmId(@PathVariable("farmId") Long farmId) {
        try {
            List<Dataset> datasets = datasetService.getByFarmId(farmId);
            return ResponseUtils.success(datasets);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取数据集列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取数据集列表
     * @param type 数据集类型
     * @return 数据集列表
     */
    @GetMapping("/type/{type}")
    public ResponseUtils.ApiResponse<List<Dataset>> getDatasetsByType(@PathVariable("type") String type) {
        try {
            List<Dataset> datasets = datasetService.getByType(type);
            return ResponseUtils.success(datasets);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取数据集列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取数据集
     * @param id 数据集ID
     * @return 数据集信息
     */
    @GetMapping("/{id}")
    public ResponseUtils.ApiResponse<Dataset> getDatasetById(@PathVariable("id") Long id) {
        try {
            Dataset dataset = datasetService.getById(id);
            return ResponseUtils.success(dataset);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取数据集失败: " + e.getMessage());
        }
    }

    /**
     * 删除数据集
     * @param id 数据集ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseUtils.ApiResponse<Boolean> deleteDataset(@PathVariable("id") Long id) {
        try {
            boolean result = datasetService.deleteDataset(id);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "删除数据集失败: " + e.getMessage());
        }
    }

}
