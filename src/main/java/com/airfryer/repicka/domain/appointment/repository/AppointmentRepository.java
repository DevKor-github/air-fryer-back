package com.airfryer.repicka.domain.appointment.repository;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.post.entity.Post;
import com.airfryer.repicka.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>
{
    // 게시글 ID, 소유자, 대여자, 약속 상태로 약속 데이터 조회
    Optional<Appointment> findByPostIdAndOwnerAndBorrowerAndState(Long postId, User owner, User borrower, AppointmentState state);

    // 게시글 ID, 반납 일시, 약속 상태로 약속 데이터 조회
    Optional<Appointment> findByPostIdAndReturnDateAndState(Long postId, LocalDateTime returnDate, AppointmentState state);

    // 어떤 게시글의 특정 구간 동안 존재하는 모든 약속 조회
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.post.id = :postId AND (
           (a.rentalDate BETWEEN :start AND :end) OR
           (a.returnDate BETWEEN :start AND :end) OR
           (a.rentalDate < :start AND a.returnDate > :end)
        )
    """)
    List<Appointment> findListOverlappingWithPeriod(
            @Param("postId") Long postId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
