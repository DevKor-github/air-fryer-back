package com.airfryer.repicka.domain.appointment.dto;

import com.airfryer.repicka.domain.appointment.FindMyAppointmentPeriod;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.post.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FindMyAppointmentPageReq
{
    @NotBlank(message = "약속 타입을 입력해주세요. (RENTAL / SALE)")
    private PostType type;

    @NotBlank(message = "검색 기간을 입력해주세요. (ALL / YEAR / SIX_MONTH / WEEK)")
    private FindMyAppointmentPeriod period;

    @NotBlank(message = "커서(약속 상태)를 입력해주세요. (CONFIRMED / IN_PROGRESS / SUCCESS)")
    private AppointmentState cursorState;

    @NotNull(message = "커서(일시)를 입력해주세요.")
    private LocalDateTime cursorDate;

    @NotNull(message = "커서(ID)를 입력해주세요.")
    private Long id;
}
