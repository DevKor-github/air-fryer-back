package com.airfryer.repicka.domain.appointment.repository;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>
{
    Optional<Appointment> findByPostAndOwnerAndBorrowerAndState(Post post, User owner, User borrower, AppointmentState state);

}
