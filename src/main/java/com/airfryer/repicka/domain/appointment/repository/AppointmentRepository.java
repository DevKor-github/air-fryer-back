package com.airfryer.repicka.domain.appointment.repository;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
import com.airfryer.repicka.domain.post.entity.PostType;
import com.airfryer.repicka.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>
{
    // 게시글 ID, 소유자, 대여자, 약속 상태로 약속 데이터 조회
    List<Appointment> findByPostIdAndOwnerIdAndRequesterIdAndState(Long postId, Long ownerId, Long requesterId, AppointmentState state);

    // 게시글 ID, 반납 일시, 약속 상태로 약속 데이터 조회
    Optional<Appointment> findByPostIdAndReturnDateAndState(Long postId, LocalDateTime returnDate, AppointmentState state);

    // 게시글 ID, 약속 상태로 약속 리스트 조회
    List<Appointment> findByPostIdAndState(Long postId, AppointmentState state);

    // 게시글, ID, 약속 상태로 반납 날짜가 가장 늦은 약속 데이터 조회
    Optional<Appointment> findTop1ByPostIdAndStateOrderByReturnDateDesc(Long postId, AppointmentState state);

    // 어떤 게시글의 특정 구간 동안 존재하는 모든 특정 상태의 약속 조회
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.post.id = :postId AND a.state = :state AND (
           (a.rentalDate BETWEEN :start AND :end) OR
           (a.returnDate BETWEEN :start AND :end) OR
           (a.rentalDate < :start AND a.returnDate > :end)
        )
    """)
    List<Appointment> findListOverlappingWithPeriod(
            @Param("postId") Long postId,
            @Param("state") AppointmentState state,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 어떤 게시글의 특정 구간 동안 존재하는 모든 특정 상태의 약속 조회
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.post.id = :postId AND a.state = :state AND (
           (a.returnDate >= :start)
        )
    """)
    List<Appointment> findListOverlappingWithPeriod(
            @Param("postId") Long postId,
            @Param("state") AppointmentState state,
            @Param("start") LocalDateTime start
    );

    // 대여자(구매자) ID, 검색 시작 날짜, 게시글 타입으로 (확정/대여중/완료) 상태인 약속 페이지 조회
    @Query(
            value = """
                SELECT a FROM Appointment a JOIN FETCH a.post p JOIN FETCH p.item i
                WHERE a.requester.id = :requesterId
                    AND a.post.postType = :type
                    AND a.rentalDate >= :start
                    AND a.state IN ('CONFIRMED', 'IN_PROGRESS', 'SUCCESS')
            """,
            countQuery = """
                SELECT COUNT(a) FROM Appointment a
                WHERE a.requester.id = :requesterId
                  AND a.post.postType = :type
                  AND a.rentalDate >= :start
                  AND a.state IN ('CONFIRMED', 'IN_PROGRESS', 'SUCCESS')
            """
    )
    Page<Appointment> findMyAppointmentPageAsRequester(Pageable pageable,
                                                       @Param("requesterId") Long requesterId,
                                                       @Param("type") PostType type,
                                                       @Param("start") LocalDateTime start);

    // 소유자 ID, 검색 시작 날짜, 게시글 타입으로 (확정/대여중/완료) 상태인 약속 페이지 조회
    @Query(
            value = """
                SELECT a FROM Appointment a JOIN FETCH a.post p JOIN FETCH p.item i
                WHERE a.owner.id = :ownerId
                    AND a.post.postType = :type
                    AND a.rentalDate >= :start
                    AND a.state IN ('CONFIRMED', 'IN_PROGRESS', 'SUCCESS')
            """,
            countQuery = """
                SELECT COUNT(a) FROM Appointment a
                WHERE a.owner.id = :ownerId
                  AND a.post.postType = :type
                  AND a.rentalDate >= :start
                  AND a.state IN ('CONFIRMED', 'IN_PROGRESS', 'SUCCESS')
            """
    )
    Page<Appointment> findMyAppointmentPageAsOwner(Pageable pageable,
                                                   @Param("ownerId") Long ownerId,
                                                   @Param("type") PostType type,
                                                   @Param("start") LocalDateTime start);

    // 레코드 수정 날짜가 특정 시점 이전인 약속 리스트 조회
    List<Appointment> findByUpdatedAtBefore(LocalDateTime localDateTime);
}
