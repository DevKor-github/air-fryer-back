package com.airfryer.repicka.domain.appointment;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public enum FindMyAppointmentSubject
{
    REQUESTER("REQUESTER", "대여자(구매자)") {
        @Override
        public Page<Appointment> findAppointmentPage(AppointmentRepository repository,
                                                     Pageable pageable,
                                                     User user,
                                                     PostType type,
                                                     LocalDateTime fromDate)
        {
            return repository.findMyAppointmentPageAsRequester(pageable, user.getId(), type, fromDate);
        }
    },
    OWNER("OWNER", "소유자") {
        @Override
        public Page<Appointment> findAppointmentPage(AppointmentRepository repository,
                                                     Pageable pageable,
                                                     User user,
                                                     PostType type,
                                                     LocalDateTime fromDate)
        {
            return repository.findMyAppointmentPageAsOwner(pageable, user.getId(), type, fromDate);
        }
    };

    private final String code;
    private final String label;

    public abstract Page<Appointment> findAppointmentPage(AppointmentRepository repository,
                                                          Pageable pageable,
                                                          User user,
                                                          PostType type,
                                                          LocalDateTime fromDate);
}
