package com.airfryer.repicka.domain.appointment.entity;

import com.airfryer.repicka.common.entity.BaseEntity;
import com.airfryer.repicka.domain.appointment.dto.OfferAppointmentReq;
import com.airfryer.repicka.domain.appointment.dto.UpdateAppointmentReq;
import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.entity.TradeMethod;
import com.airfryer.repicka.domain.user.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Appointment extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제품
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item")
    private Item item;

    // 대여자(구매자)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester")
    private User requester;

    // 소유자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    private User owner;

    // 생성자
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator")
    private User creator;

    // 약속 종류
    @NotNull
    @Enumerated(EnumType.STRING)
    private AppointmentType type;

    // 약속 상태
    @NotNull
    @Enumerated(EnumType.STRING)
    private AppointmentState state;

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

    // 거래 방식
    @NotNull
    @Enumerated(EnumType.STRING)
    private TradeMethod tradeMethod;

    /// 약속 데이터 생성

    public static Appointment of(Item item, User requester, OfferAppointmentReq dto, boolean isRental)
    {
        return Appointment.builder()
                .item(item)
                .requester(requester)
                .owner(item.getOwner())
                .creator(requester)
                .type(isRental ? AppointmentType.RENTAL : AppointmentType.SALE)
                .state(AppointmentState.PENDING)
                .rentalDate(dto.getStartDate())
                .returnDate(isRental ? dto.getEndDate() : null)
                .rentalLocation(dto.getStartLocation().trim())
                .returnLocation(isRental ? dto.getEndLocation() : null)
                .price(dto.getPrice())
                .deposit(isRental ? dto.getDeposit() : 0)
                .tradeMethod(dto.getTradeMethod())
                .build();
    }

    /// 약속 데이터 수정

    public void update(User user, UpdateAppointmentReq dto, boolean isRental)
    {
        this.creator = user;
        this.state = AppointmentState.PENDING;

        this.rentalLocation = dto.getRentalLocation();
        this.returnLocation = isRental ? dto.getReturnLocation() : null;
        this.rentalDate = dto.getRentalDate();
        this.returnDate = isRental ? dto.getReturnDate() : null;
        this.price = dto.getPrice();
        this.deposit = isRental ? dto.getDeposit() : 0;
        this.tradeMethod = dto.getTradeMethod();
    }

    public void update(LocalDateTime returnDate, String returnLocation)
    {
        this.returnDate = returnDate;
        this.returnLocation = returnLocation;
    }

    /// 약속 확정

    public void confirm() {
        this.state = AppointmentState.CONFIRMED;
    }

    /// 약속 대여중

    public void inProgress() {
        this.state = AppointmentState.IN_PROGRESS;
    }

    /// 약속 취소

    public void cancel() {
        this.state = AppointmentState.CANCELLED;
    }

    /// 약속 만료

    public void expire() {
        this.state = AppointmentState.EXPIRED;
    }

    /// 약속 완료

    public void success() {
        this.state = AppointmentState.SUCCESS;
    }
}
