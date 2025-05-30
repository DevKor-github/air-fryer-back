package com.airfryer.repicka.domain.appointment.entity;

import com.airfryer.repicka.common.entity.BaseEntity;
import com.airfryer.repicka.domain.appointment.dto.OfferAppointmentInPostReq;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "appointment"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Appointment extends BaseEntity
{
    // 약속 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post")
    private Post post;

    // 생성자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator")
    private User creator;

    // 소유자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    private User owner;

    // 대여자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower")
    private User borrower;

    // 대여 장소
    @Column(length = 255)
    private String rentalLocation;

    // 반납 장소
    @Column(length = 255)
    private String returnLocation;

    // 대여 일시
    @NotNull
    private LocalDateTime rentalDate;

    // 반납 일시
    private LocalDateTime returnDate;

    // 대여료/판매값
    @NotNull
    private int price;

    // 보증금
    @NotNull
    @Builder.Default
    private int deposit = 0;

    // 약속 진행 상태
    @NotNull
    @Enumerated(EnumType.STRING)
    private AppointmentState state;

    // 약속 데이터 수정
    public void updateAppointment(OfferAppointmentInPostReq dto)
    {
        this.rentalLocation = dto.getRentalLocation();
        this.returnLocation = dto.getReturnLocation();
        this.rentalDate = dto.getRentalDate();
        this.returnDate = dto.getReturnDate();
        this.price = dto.getPrice();
        this.deposit = dto.getDeposit();
    }
}
