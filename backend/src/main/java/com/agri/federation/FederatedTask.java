package com.agri.federation;

import java.util.Date;

public class FederatedTask {
    private String id;
    private String taskName;
    private String algorithm;
    private int totalRounds;
    private int expectedClients;
    private int currentRound;
    private String status;
    private Date createTime;
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public int getTotalRounds() { return totalRounds; }
    public void setTotalRounds(int totalRounds) { this.totalRounds = totalRounds; }
    public int getExpectedClients() { return expectedClients; }
    public void setExpectedClients(int expectedClients) { this.expectedClients = expectedClients; }
    public int getCurrentRound() { return currentRound; }
    public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
