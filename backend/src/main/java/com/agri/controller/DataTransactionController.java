package com.agri.controller;

import com.agri.model.DataTransaction;
import com.agri.service.DataTransactionService;
import com.agri.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 数据交易控制器
 */
@RestController
@RequestMapping("/data-transaction")
public class DataTransactionController {

    @Autowired
    private DataTransactionService dataTransactionService;

    /**
     * 创建交易
     * @param dataTransaction 交易信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseUtils.ApiResponse<DataTransaction> createTransaction(@RequestBody DataTransaction dataTransaction) {
        try {
            boolean result = dataTransactionService.createTransaction(dataTransaction);
            if (result) {
                return ResponseUtils.success(dataTransaction);
            } else {
                return ResponseUtils.error(500, "创建交易失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "创建交易失败: " + e.getMessage());
        }
    }

    /**
     * 根据交易ID获取交易信息
     * @param id 交易ID
     * @return 交易信息
     */
    @GetMapping("/{id}")
    public ResponseUtils.ApiResponse<DataTransaction> getById(@PathVariable("id") Long id) {
        try {
            DataTransaction dataTransaction = dataTransactionService.getById(id);
            return ResponseUtils.success(dataTransaction);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "获取交易信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据买家ID获取交易列表
     * @param buyerId 买家ID
     * @return 交易列表
     */
    @GetMapping("/buyer/{buyerId}")
    public ResponseUtils.ApiResponse<List<DataTransaction>> getByBuyerId(@PathVariable("buyerId") Long buyerId) {
        try {
            List<DataTransaction> transactionList = dataTransactionService.getByBuyerId(buyerId);
            return ResponseUtils.success(transactionList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "获取交易列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据卖家ID获取交易列表
     * @param sellerId 卖家ID
     * @return 交易列表
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseUtils.ApiResponse<List<DataTransaction>> getBySellerId(@PathVariable("sellerId") Long sellerId) {
        try {
            List<DataTransaction> transactionList = dataTransactionService.getBySellerId(sellerId);
            return ResponseUtils.success(transactionList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "获取交易列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新交易状态
     * @param id 交易ID
     * @param status 状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status")
    public ResponseUtils.ApiResponse<Boolean> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        try {
            boolean result = dataTransactionService.updateStatus(id, status);
            return ResponseUtils.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "更新状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有交易记录（一级管理员使用）
     * @return 交易列表
     */
    @GetMapping("/all")
    public ResponseUtils.ApiResponse<List<DataTransaction>> getAllTransactions() {
        try {
            List<DataTransaction> transactionList = dataTransactionService.getAllTransactions();
            return ResponseUtils.success(transactionList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.error(500, "获取交易记录失败: " + e.getMessage());
        }
    }

}
