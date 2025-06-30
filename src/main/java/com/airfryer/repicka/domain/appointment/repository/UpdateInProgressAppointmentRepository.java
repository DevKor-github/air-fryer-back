package com.airfryer.repicka.domain.appointment.repository;

import com.airfryer.repicka.domain.appointment.entity.UpdateInProgressAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpdateInProgressAppointmentRepository extends JpaRepository<UpdateInProgressAppointment, Long> {
}
