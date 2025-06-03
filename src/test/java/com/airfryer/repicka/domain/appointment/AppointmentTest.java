package com.airfryer.repicka.domain.appointment;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.util.CreateEntityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class AppointmentTest
{
    @Autowired AppointmentRepository appointmentRepository;

    @Autowired CreateEntityUtil createEntityUtil;

    @Test
    @DisplayName("Appointment 엔티티 생성 테스트")
    void createAppointment()
    {
        /// Given

        Appointment appointment = createEntityUtil.createAppointment();

        /// Then

        Appointment findAppointment = appointmentRepository.findById(appointment.getId()).orElse(null);

        assertThat(findAppointment).isNotNull();
        assertThat(findAppointment.getPost()).isEqualTo(appointment.getPost());
        assertThat(findAppointment.getCreator()).isEqualTo(appointment.getCreator());
        assertThat(findAppointment.getOwner()).isEqualTo(appointment.getOwner());
        assertThat(findAppointment.getRequester()).isEqualTo(appointment.getRequester());
        assertThat(findAppointment.getRentalLocation()).isEqualTo(appointment.getRentalLocation());
        assertThat(findAppointment.getReturnLocation()).isEqualTo(appointment.getReturnLocation());
        assertThat(findAppointment.getRentalDate()).isEqualTo(appointment.getRentalDate());
        assertThat(findAppointment.getReturnDate()).isEqualTo(appointment.getReturnDate());
        assertThat(findAppointment.getPrice()).isEqualTo(appointment.getPrice());
        assertThat(findAppointment.getDeposit()).isEqualTo(appointment.getDeposit()).isEqualTo(0);
        assertThat(findAppointment.getState()).isEqualTo(appointment.getState());
    }
}
