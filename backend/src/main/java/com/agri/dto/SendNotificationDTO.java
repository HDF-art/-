package com.agri.dto;

import lombok.Data;

import java.util.List;

@Data
public class SendNotificationDTO {
    private String title;
    private String content;
    private Long senderId;
    private Integer receiverType;
    private Long farmId;
    private List<Long> specificUserIds;
}
