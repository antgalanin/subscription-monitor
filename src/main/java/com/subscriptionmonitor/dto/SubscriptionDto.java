package com.subscriptionmonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {
    private UUID id;
    private String name;
    private UUID userId;
    private UUID categoryId;
    private UUID paymentId;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
