package com.airfryer.repicka.domain.appointment.entity;

import com.airfryer.repicka.common.entity.BaseEntity;
import com.airfryer.repicka.domain.appointment.dto.OfferAppointmentInRentalPostReq;
import com.airfryer.repicka.domain.appointment.dto.OfferAppointmentInSalePostReq;
import com.airfryer.repicka.domain.appointment.dto.OfferToUpdateAppointmentReq;
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

    // 대여자(구매자)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester")
    private User requester;

    // 대여(구매) 일시
    @NotNull
    private LocalDateTime rentalDate;

    // 반납 일시
    private LocalDateTime returnDate;

    // 대여(구매) 장소
    @Column(length = 255)
    private String rentalLocation;

    // 반납 장소
    @Column(length = 255)
    private String returnLocation;

    // 대여료(판매값)
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

    /// 약속 데이터 수정

    public void updateAppointment(OfferAppointmentInRentalPostReq dto)
    {
        this.rentalLocation = dto.getRentalLocation();
        this.returnLocation = dto.getReturnLocation();
        this.rentalDate = dto.getRentalDate();
        this.returnDate = dto.getReturnDate();
        this.price = dto.getPrice();
        this.deposit = dto.getDeposit();
    }

    public void updateAppointment(OfferAppointmentInSalePostReq dto)
    {
        this.rentalLocation = dto.getSaleLocation();
        this.returnLocation = null;
        this.rentalDate = dto.getSaleDate();
        this.returnDate = null;
        this.price = dto.getPrice();
        this.deposit = 0;
    }

    public void updateAppointment(OfferToUpdateAppointmentReq dto, boolean isRental)
    {
        this.rentalLocation = dto.getRentalLocation();
        this.returnLocation = isRental ? dto.getReturnLocation() : null;
        this.rentalDate = dto.getRentalDate();
        this.returnDate = isRental ? dto.getReturnDate() : null;
        this.price = dto.getPrice();
        this.deposit = isRental ? dto.getDeposit() : 0;
        this.state = AppointmentState.PENDING;
    }

    /// 약속 확정

    public void confirmAppointment() {
        this.state = AppointmentState.CONFIRMED;
    }

    /// 약속 취소

    public void cancelAppointment() {
        this.state = AppointmentState.CANCELLED;
    }

    /// 약속 데이터 복사

    public Appointment clone(User creator)
    {
        return Appointment.builder()
                .post(this.post)
                .creator(creator)
                .owner(this.owner)
                .requester(this.requester)
                .rentalDate(this.rentalDate)
                .returnDate(this.returnDate)
                .rentalLocation(this.rentalLocation)
                .returnLocation(this.returnLocation)
                .price(this.price)
                .deposit(this.deposit)
                .state(this.state)
                .build();
    }
}
