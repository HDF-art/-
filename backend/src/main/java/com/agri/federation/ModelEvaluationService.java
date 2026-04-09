package com.agri.federation;

import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 模型评估服务
 * 提供多种评估指标
 */
@Service
public class ModelEvaluationService {
    
    /**
     * 分类评估指标
     */
    public Map<String, Object> evaluateClassification(double[] predictions, double[] labels) {
        Map<String, Object> metrics = new HashMap<>();
        
        // 准确率
        int correct = 0;
        for (int i = 0; i < predictions.length; i++) {
            if (Math.round(predictions[i]) == labels[i]) {
                correct++;
            }
        }
        double accuracy = (double) correct / predictions.length;
        metrics.put("accuracy", accuracy);
        
        // 计算混淆矩阵
        Map<String, Integer> confusion = calculateConfusionMatrix(predictions, labels);
        metrics.put("confusionMatrix", confusion);
        
        // 精确率、召回率、F1
        double[] precision = calculatePrecision(predictions, labels);
        double[] recall = calculateRecall(predictions, labels);
        double[] f1 = calculateF1(precision, recall);
        
        metrics.put("precision", precision);
        metrics.put("recall", recall);
        metrics.put("f1Score", f1);
        metrics.put("macroF1", Arrays.stream(f1).average().orElse(0));
        
        // AUC-ROC (简化版)
        double auc = calculateAUC(predictions, labels);
        metrics.put("auc", auc);
        
        return metrics;
    }
    
    /**
     * 回归评估指标
     */
    public Map<String, Object> evaluateRegression(double[] predictions, double[] labels) {
        Map<String, Object> metrics = new HashMap<>();
        
        // MSE
        double mse = 0;
        for (int i = 0; i < predictions.length; i++) {
            double diff = predictions[i] - labels[i];
            mse += diff * diff;
        }
        mse /= predictions.length;
        metrics.put("mse", mse);
        
        // RMSE
        metrics.put("rmse", Math.sqrt(mse));
        
        // MAE
        double mae = 0;
        for (int i = 0; i < predictions.length; i++) {
            mae += Math.abs(predictions[i] - labels[i]);
        }
        mae /= predictions.length;
        metrics.put("mae", mae);
        
        // R²
        double mean = Arrays.stream(labels).average().orElse(0);
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < predictions.length; i++) {
            ssTot += (labels[i] - mean) * (labels[i] - mean);
            ssRes += (predictions[i] - labels[i]) * (predictions[i] - labels[i]);
        }
        double r2 = 1 - (ssRes / ssTot);
        metrics.put("r2", r2);
        
        // MAPE
        double mape = 0;
        for (int i = 0; i < predictions.length; i++) {
            if (labels[i] != 0) {
                mape += Math.abs((predictions[i] - labels[i]) / labels[i]);
            }
        }
        mape = (mape / predictions.length) * 100;
        metrics.put("mape", mape);
        
        return metrics;
    }
    
    /**
     * 计算混淆矩阵
     */
    private Map<String, Integer> calculateConfusionMatrix(double[] predictions, double[] labels) {
        Map<String, Integer> confusion = new HashMap<>();
        
        for (int i = 0; i < predictions.length; i++) {
            int pred = (int) Math.round(predictions[i]);
            int label = (int) labels[i];
            String key = pred + "_" + label;
            confusion.put(key, confusion.getOrDefault(key, 0) + 1);
        }
        
        return confusion;
    }
    
    /**
     * 计算精确率
     */
    private double[] calculatePrecision(double[] predictions, double[] labels) {
        Map<Integer, Integer> tp = new HashMap<>();
        Map<Integer, Integer> fp = new HashMap<>();
        
        for (int i = 0; i < predictions.length; i++) {
            int pred = (int) Math.round(predictions[i]);
            int label = (int) labels[i];
            
            if (pred == label) {
                tp.put(pred, tp.getOrDefault(pred, 0) + 1);
            } else {
                fp.put(pred, fp.getOrDefault(pred, 0) + 1);
            }
        }
        
        // 获取类别数
        Set<Integer> classes = new HashSet<>();
        for (double l : labels) classes.add((int) l);
        
        double[] precision = new double[classes.size()];
        int idx = 0;
        for (int cls : classes) {
            int tpVal = tp.getOrDefault(cls, 0);
            int fpVal = fp.getOrDefault(cls, 0);
            precision[idx++] = (tpVal + fpVal) > 0 ? (double) tpVal / (tpVal + fpVal) : 0;
        }
        
        return precision;
    }
    
    /**
     * 计算召回率
     */
    private double[] calculateRecall(double[] predictions, double[] labels) {
        Map<Integer, Integer> tp = new HashMap<>();
        Map<Integer, Integer> fn = new HashMap<>();
        
        for (int i = 0; i < predictions.length; i++) {
            int pred = (int) Math.round(predictions[i]);
            int label = (int) labels[i];
            
            if (pred == label) {
                tp.put(label, tp.getOrDefault(label, 0) + 1);
            } else {
                fn.put(label, fn.getOrDefault(label, 0) + 1);
            }
        }
        
        Set<Integer> classes = new HashSet<>();
        for (double l : labels) classes.add((int) l);
        
        double[] recall = new double[classes.size()];
        int idx = 0;
        for (int cls : classes) {
            int tpVal = tp.getOrDefault(cls, 0);
            int fnVal = fn.getOrDefault(cls, 0);
            recall[idx++] = (tpVal + fnVal) > 0 ? (double) tpVal / (tpVal + fnVal) : 0;
        }
        
        return recall;
    }
    
    /**
     * 计算F1分数
     */
    private double[] calculateF1(double[] precision, double[] recall) {
        double[] f1 = new double[precision.length];
        for (int i = 0; i < precision.length; i++) {
            if (precision[i] + recall[i] > 0) {
                f1[i] = 2 * precision[i] * recall[i] / (precision[i] + recall[i]);
            }
        }
        return f1;
    }
    
    /**
     * 计算AUC-ROC
     */
    private double calculateAUC(double[] predictions, double[] labels) {
        // 简化版AUC计算
        int correct = 0;
        int total = 0;
        
        for (int i = 0; i < predictions.length; i++) {
            for (int j = i + 1; j < predictions.length; j++) {
                if ((predictions[i] > predictions[j] && labels[i] > labels[j]) ||
                    (predictions[i] < predictions[j] && labels[i] < labels[j])) {
                    correct++;
                }
                total++;
            }
        }
        
        return total > 0 ? (double) correct / total : 0;
    }
}
