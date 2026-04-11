package com.agri.federation;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Arrays;

@Service
public class DifferentialPrivacyService {
    
    private double defaultEpsilon = 1.0;
    private double defaultDelta = 1e-5;
    private final SecureRandom secureRandom = new SecureRandom();
    
    public double[] addLaplaceNoise(double[] data, double epsilon) {
        double[] noisyData = new double[data.length];
        double scale = 1.0 / epsilon;
        
        for (int i = 0; i < data.length; i++) {
            noisyData[i] = data[i] + laplaceNoise(scale);
        }
        
        return noisyData;
    }
    
    public double[] addGaussianNoise(double[] data, double epsilon, double delta) {
        double[] noisyData = new double[data.length];
        double sigma = Math.sqrt(2 * Math.log(1.25 / delta)) / epsilon;
        
        for (int i = 0; i < data.length; i++) {
            noisyData[i] = data[i] + gaussianNoise(sigma);
        }
        
        return noisyData;
    }
    
    private double laplaceNoise(double scale) {
        double u = secureRandom.nextDouble() - 0.5;
        return -scale * Math.signum(u) * Math.log(1 - 2 * Math.abs(u));
    }
    
    private double gaussianNoise(double sigma) {
        double u1 = secureRandom.nextDouble();
        double u2 = secureRandom.nextDouble();
        double z = Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
        return sigma * z;
    }
    
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
    
    public double[] clipGradient(double[] gradients, double maxNorm) {
        double norm = Math.sqrt(Arrays.stream(gradients).map(x -> x * x).sum());
        
        if (norm > maxNorm) {
            double scale = maxNorm / norm;
            return Arrays.stream(gradients).map(x -> x * scale).toArray();
        }
        
        return gradients;
    }
    
    public double[][] batchClipGradient(double[][] gradients, double maxNorm) {
        return Arrays.stream(gradients)
                .map(g -> clipGradient(g, maxNorm))
                .toArray(double[][]::new);
    }
    
    public double composeEpsilon(double epsilon1, double epsilon2) {
        return epsilon1 + epsilon2;
    }
    
    public double composeParallelEpsilon(double epsilon, int mechanisms) {
        return epsilon;
    }
    
    public double getDefaultEpsilon() { return defaultEpsilon; }
    public void setDefaultEpsilon(double epsilon) { this.defaultEpsilon = epsilon; }
    public double getDefaultDelta() { return defaultDelta; }
    public void setDefaultDelta(double delta) { this.defaultDelta = delta; }
}
