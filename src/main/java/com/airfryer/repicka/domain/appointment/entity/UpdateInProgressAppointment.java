package com.airfryer.repicka.domain.appointment.entity;

import com.airfryer.repicka.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class UpdateInProgressAppointment
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 약속
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment")
    private Appointment appointment;

    // 생성자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator")
    private User creator;

    // 반납 일시
    @NotNull
    private LocalDateTime returnDate;

    // 반납 장소
    @NotNull
    @Column(length = 255)
    private String returnLocation;
}
