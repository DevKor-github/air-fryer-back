package com.airfryer.repicka.common.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.airfryer.repicka.domain.item.entity.TransactionType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentTask {
    private Long appointmentId; // 예약 아이디
    private TransactionType transactionType; // 거래 유형
    private Long ownerId; // 소유자 아이디
    private Long requesterId; // 요청자 아이디
    private String taskType; // "EXPIRE", "REMIND"
} 