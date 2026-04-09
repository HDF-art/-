package com.agri.service;

import com.agri.model.DataTransaction;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 数据交易服务接口
 */
public interface DataTransactionService extends IService<DataTransaction> {

    /**
     * 创建交易
     * @param dataTransaction 交易信息
     * @return 创建结果
     */
    boolean createTransaction(DataTransaction dataTransaction);

    /**
     * 根据交易ID获取交易信息
     * @param id 交易ID
     * @return 交易信息
     */
    DataTransaction getById(Long id);

    /**
     * 根据买家ID获取交易列表
     * @param buyerId 买家ID
     * @return 交易列表
     */
    List<DataTransaction> getByBuyerId(Long buyerId);

    /**
     * 根据卖家ID获取交易列表
     * @param sellerId 卖家ID
     * @return 交易列表
     */
    List<DataTransaction> getBySellerId(Long sellerId);

    /**
     * 更新交易状态
     * @param id 交易ID
     * @param status 状态
     * @return 更新结果
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 获取所有交易记录（一级管理员使用）
     * @return 交易列表
     */
    List<DataTransaction> getAllTransactions();

}
