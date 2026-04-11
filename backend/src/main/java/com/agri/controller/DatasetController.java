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
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/datasets")
public class DatasetController {

    @Autowired
    private DatasetService datasetService;

    @Value("${file.upload-dir:D:/agri-platform/upload/}")
    private String uploadDirBase;
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex);
        }
        return "";
    }
    
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    private Path validateAndResolvePath(Long farmId, String filename) {
        Path basePath = Paths.get(uploadDirBase, "datasets").toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(farmId.toString()).resolve(filename).normalize();
        
        if (!targetPath.startsWith(basePath)) {
            throw new SecurityException("检测到目录遍历攻击");
        }
        
        return targetPath;
    }

    @PostMapping("/upload")
    public ResponseUtils.ApiResponse<Dataset> uploadDataset(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("description") String description,
            @RequestParam("uploaderId") Long uploaderId,
            @RequestParam("farmId") Long farmId) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String safeName = sanitizeFilename(originalFilename);
            String fileName = UUID.randomUUID().toString() + extension;
            
            Path targetPath = validateAndResolvePath(farmId, fileName);
            Files.createDirectories(targetPath.getParent());
            
            file.transferTo(targetPath.toFile());

            Dataset dataset = new Dataset();
            dataset.setName(name);
            dataset.setPath(targetPath.toString());
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
        } catch (SecurityException e) {
            return ResponseUtils.error(403, "操作被拒绝");
        } catch (IOException e) {
            return ResponseUtils.error(500, "文件上传失败");
        } catch (Exception e) {
            return ResponseUtils.error(500, "上传失败");
        }
    }

    @GetMapping
    public ResponseUtils.ApiResponse<List<Dataset>> getDatasetList() {
        try {
            List<Dataset> datasets = datasetService.list();
            return ResponseUtils.success(datasets);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取数据集列表失败");
        }
    }

    @GetMapping("/uploader/{uploaderId}")
    public ResponseUtils.ApiResponse<List<Dataset>> getDatasetsByUploaderId(@PathVariable("uploaderId") Long uploaderId) {
        try {
            List<Dataset> datasets = datasetService.getByUploaderId(uploaderId);
            return ResponseUtils.success(datasets);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取数据集列表失败");
        }
    }

    @GetMapping("/farm/{farmId}")
    public ResponseUtils.ApiResponse<List<Dataset>> getDatasetsByFarmId(@PathVariable("farmId") Long farmId) {
        try {
            List<Dataset> datasets = datasetService.getByFarmId(farmId);
            return ResponseUtils.success(datasets);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取数据集列表失败");
        }
    }

    @GetMapping("/type/{type}")
    public ResponseUtils.ApiResponse<List<Dataset>> getDatasetsByType(@PathVariable("type") String type) {
        try {
            List<Dataset> datasets = datasetService.getByType(type);
            return ResponseUtils.success(datasets);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取数据集列表失败");
        }
    }

    @GetMapping("/{id}")
    public ResponseUtils.ApiResponse<Dataset> getDatasetById(@PathVariable("id") Long id) {
        try {
            Dataset dataset = datasetService.getById(id);
            return ResponseUtils.success(dataset);
        } catch (Exception e) {
            return ResponseUtils.error(500, "获取数据集失败");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseUtils.ApiResponse<Boolean> deleteDataset(@PathVariable("id") Long id) {
        try {
            boolean result = datasetService.deleteDataset(id);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            return ResponseUtils.error(500, "删除数据集失败");
        }
    }
}
