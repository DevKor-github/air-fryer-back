package com.airfryer.repicka.domain.appointment.repository;

import com.airfryer.repicka.domain.appointment.entity.UpdateInProgressAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UpdateInProgressAppointmentRepository extends JpaRepository<UpdateInProgressAppointment, Long>
{
    // 약속 ID로 대여중 약속 변경 요청 데이터 조회
    List<UpdateInProgressAppointment> findByAppointmentId(Long appointmentId);

    // 약속 ID 리스트로 대여중 약속 변경 요청 데이터 조회
    List<UpdateInProgressAppointment> findByAppointmentIdIn(List<Long> appointmentIds);

    // 약속 ID, 생성한 사용자 ID로 대여중 약속 변경 요청 데이터 조회
    Optional<UpdateInProgressAppointment> findByAppointmentIdAndCreatorId(Long appointmentId, Long creatorId);
}
