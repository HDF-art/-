package com.agri.federation;

import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.Arrays;

/**
 * 差分隐私服务
 * 提供Laplace和Gaussian噪声添加机制
 */
@Service
public class DifferentialPrivacyService {
    
    private double defaultEpsilon = 1.0;  // 隐私预算
    private double defaultDelta = 1e-5;   // 失败概率
    private Random random = new Random();
    
    /**
     * 添加Laplace噪声（ε-差分隐私）
     * @param data 原始数据
     * @param epsilon 隐私预算（越小越隐私）
     * @return 加噪后的数据
     */
    public double[] addLaplaceNoise(double[] data, double epsilon) {
        double[] noisyData = new double[data.length];
        double scale = 1.0 / epsilon;
        
        for (int i = 0; i < data.length; i++) {
            noisyData[i] = data[i] + laplaceNoise(scale);
        }
        
        return noisyData;
    }
    
    /**
     * 添加Gaussian噪声（(ε,δ)-差分隐私）
     */
    public double[] addGaussianNoise(double[] data, double epsilon, double delta) {
        double[] noisyData = new double[data.length];
        double sigma = Math.sqrt(2 * Math.log(1.25 / delta)) / epsilon;
        
        for (int i = 0; i < data.length; i++) {
            noisyData[i] = data[i] + gaussianNoise(sigma);
        }
        
        return noisyData;
    }
    
    /**
     * 生成Laplace分布噪声
     */
    private double laplaceNoise(double scale) {
        double u = random.nextDouble() - 0.5;
        return -scale * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
    }
    
    /**
     * 生成Gaussian分布噪声
     */
    private double gaussianNoise(double sigma) {
        // Box-Muller transform
        double u1 = random.nextDouble();
        double u2 = random.nextDouble();
        double z = Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
        return sigma * z;
    }
    
    /**
     * 批量添加差分隐私
     */
    public double[][] batchAddNoise(double[][] gradients, String noiseType, double epsilon) {
        double[][] noisyGradients = new double[gradients.length][];
        
        for (int i = 0; i < gradients.length; i++) {
            if ("gaussian".equalsIgnoreCase(noiseType)) {
                noisyGradients[i] = addGaussianNoise(gradients[i], epsilon, defaultDelta);
            } else {
                noisyGradients[i] = addLaplaceNoise(gradients[i], epsilon);
            }
        }
        
        return noisyGradients;
    }
    
    /**
     * 梯度裁剪
     * @param gradients 原始梯度
     * @param maxNorm 最大范数
     */
    public double[] clipGradient(double[] gradients, double maxNorm) {
        double norm = Math.sqrt(Arrays.stream(gradients).map(x -> x * x).sum());
        
        if (norm > maxNorm) {
            double scale = maxNorm / norm;
            return Arrays.stream(gradients).map(x -> x * scale).toArray();
        }
        
        return gradients;
    }
    
    /**
     * 梯度裁剪（批量）
     */
    public double[][] batchClipGradient(double[][] gradients, double maxNorm) {
        return Arrays.stream(gradients)
                .map(g -> clipGradient(g, maxNorm))
                .toArray(double[][]::new);
    }
    
    /**
     * 组合隐私预算（顺序组合）
     */
    public double composeEpsilon(double epsilon1, double epsilon2) {
        return epsilon1 + epsilon2;
    }
    
    /**
     * 组合隐私预算（并行组合）
     */
    public double composeParallelEpsilon(double epsilon, int mechanisms) {
        return epsilon;
    }
    
    // Getters and Setters
    public double getDefaultEpsilon() { return defaultEpsilon; }
    public void setDefaultEpsilon(double epsilon) { this.defaultEpsilon = epsilon; }
    public double getDefaultDelta() { return defaultDelta; }
    public void setDefaultDelta(double delta) { this.defaultDelta = delta; }
}
