package com.agri.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class FedLabTaskConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taskName;
    private String dataset;
    private String algorithm;
    private String serverIp;
    private Integer serverPort = 3002;
    private Integer worldSize;
    private Integer rank = 0;
    private Integer communicationRounds = 10;
    private Integer localEpochs = 1;
    private String modelType;
}
