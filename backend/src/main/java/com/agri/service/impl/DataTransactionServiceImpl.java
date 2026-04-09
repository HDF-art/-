package com.agri.service.impl;

import com.agri.mapper.DataTransactionMapper;
import com.agri.model.DataTransaction;
import com.agri.service.DataTransactionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据交易服务实现类
 */
@Service
public class DataTransactionServiceImpl extends ServiceImpl<DataTransactionMapper, DataTransaction> implements DataTransactionService {

    @Autowired
    private DataTransactionMapper dataTransactionMapper;

    @Override
    public boolean createTransaction(DataTransaction dataTransaction) {
        dataTransaction.setStatus(0); // 待确认
        dataTransaction.setTransactionTime(LocalDateTime.now());
        dataTransaction.setCreatedAt(LocalDateTime.now());
        dataTransaction.setUpdatedAt(LocalDateTime.now());
        return save(dataTransaction);
    }

    @Override
    public DataTransaction getById(Long id) {
        return super.getById(id);
    }

    @Override
    public List<DataTransaction> getByBuyerId(Long buyerId) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataTransaction>()
                .eq("buyer_id", buyerId)
                .orderByDesc("created_at"));
    }

    @Override
    public List<DataTransaction> getBySellerId(Long sellerId) {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataTransaction>()
                .eq("seller_id", sellerId)
                .orderByDesc("created_at"));
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        DataTransaction dataTransaction = new DataTransaction();
        dataTransaction.setId(id);
        dataTransaction.setStatus(status);
        dataTransaction.setUpdatedAt(LocalDateTime.now());
        if (status == 2) { // 已完成
            dataTransaction.setCompletionTime(LocalDateTime.now());
        }
        return updateById(dataTransaction);
    }

    @Override
    public List<DataTransaction> getAllTransactions() {
        return list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataTransaction>()
                .orderByDesc("created_at"));
    }

}
