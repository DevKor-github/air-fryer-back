package com.airfryer.repicka.common.util.validation;

import com.airfryer.repicka.domain.user.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class AppointmentValidator
{
    // 반납 일사가 대여 일시의 이후인지 확인
    public boolean isRentalDateEarlierThanReturnDate(LocalDateTime rentalDate, LocalDateTime returnDate) {
        return returnDate.isAfter(rentalDate);
    }

    // 장소 형식 체크
    public boolean checkLocationFormat(String location) {
        return location.length() < 255;
    }

    // 소유자와 대여자가 동일하지 않은지 확인
    public boolean isOwnerAndBorrowerDifferent(User owner, User borrower) {
        return !Objects.equals(owner.getId(), borrower.getId());
    }
}
