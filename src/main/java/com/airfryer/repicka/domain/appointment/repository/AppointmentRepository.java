package com.airfryer.repicka.domain.appointment.repository;

import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.AppointmentState;
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
    // 게시글 ID, 소유자 ID, 대여자 ID로 약속 데이터 조회
    List<Appointment> findByItemIdAndOwnerIdAndRequesterId(
            Long itemId,
            Long ownerId,
            Long requesterId
    );

    // 제품 ID, 소유자 ID, 대여자 ID, 약속 상태 리스트로 약속 데이터 조회
    List<Appointment> findByItemIdAndOwnerIdAndRequesterIdAndStateIn(
            Long itemId,
            Long ownerId,
            Long requesterId,
            List<AppointmentState> state
    );

    // 제품 ID, 약속 상태로 반납 날짜가 가장 늦은 약속 데이터 조회
    Optional<Appointment> findTop1ByItemIdAndStateOrderByReturnDateDesc(Long ItemId, AppointmentState state);

    /// 어떤 게시글의 특정 구간 동안 존재하며 특정 상태에 속하는 특정 타입의 약속 조회

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.item.id = :itemId AND a.state IN :state AND a.type = 'RENTAL' AND (
           (a.rentalDate > :start AND a.rentalDate < :end) OR
           (a.returnDate > :start AND a.returnDate < :end) OR
           (a.rentalDate < :start AND a.returnDate > :end)
        )
    """)
    List<Appointment> findRentalListOverlappingWithPeriod(
            @Param("itemId") Long itemId,
            @Param("state") List<AppointmentState> state,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.item.id = :itemId AND a.state IN :state AND a.type = 'RENTAL' AND (
           (a.returnDate >= :start)
        )
    """)
    List<Appointment> findRentalListOverlappingWithPeriod(
            @Param("itemId") Long itemId,
            @Param("state") List<AppointmentState> state,
            @Param("start") LocalDateTime start
    );

    /// 대여자 ID, 검색 시작 날짜, 게시글 타입으로 (확정/대여중/완료) 상태인 약속 리스트 조회

    // 커서 기반 페이지네이션 (cursor: appointmentState, rentalDate, id)
    // AppointmentState 기준 : CONFIRMED > IN_PROGRESS > SUCCESS
    // 동일한 AppointmentState 내에서는 rentalDate 오름차순
    // 그것까지 동일하면 ID 오름차순
    @Query(
        value = """
            SELECT a.* FROM appointment a JOIN item i ON a.item = i.id
            WHERE a.requester = :requesterId
                AND a.rental_date >= :start
                AND a.state IN ('CONFIRMED', 'IN_PROGRESS', 'SUCCESS')
                AND (
                    CASE a.state
                        WHEN 'CONFIRMED' THEN 1
                        WHEN 'IN_PROGRESS' THEN 2
                        WHEN 'SUCCESS' THEN 3
                        ELSE 4
                    END > CASE :cursorState
                        WHEN 'CONFIRMED' THEN 1
                        WHEN 'IN_PROGRESS' THEN 2
                        WHEN 'SUCCESS' THEN 3
                        ELSE 4
                    END
                    OR (
                        CASE a.state
                            WHEN 'CONFIRMED' THEN 1
                            WHEN 'IN_PROGRESS' THEN 2
                            WHEN 'SUCCESS' THEN 3
                            ELSE 4
                        END = CASE :cursorState
                            WHEN 'CONFIRMED' THEN 1
                            WHEN 'IN_PROGRESS' THEN 2
                            WHEN 'SUCCESS' THEN 3
                            ELSE 4
                        END
                        AND (
                            a.rental_date > :cursorDate
                            OR (a.rental_date = :cursorDate AND a.id >= :cursorId)
                        )
                    )
                )
            ORDER BY
                CASE a.state
                    WHEN 'CONFIRMED' THEN 1
                    WHEN 'IN_PROGRESS' THEN 2
                    WHEN 'SUCCESS' THEN 3
                    ELSE 4
                END ASC,
                a.rental_date ASC,
                a.id ASC
            LIMIT :limit
        """, nativeQuery = true
    )
    List<Appointment> findMyAppointmentPageAsRequester(
            @Param("requesterId") Long requesterId,
            @Param("start") LocalDateTime start,
            @Param("cursorState") String cursorState,
            @Param("cursorDate") LocalDateTime cursorDate,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit
    );

    // 첫 페이지 조회
    @Query(
            value = """
            SELECT a.* FROM appointment a JOIN item i ON a.item = i.id
            WHERE a.requester = :requesterId
                AND a.rental_date >= :start
                AND a.state IN ('CONFIRMED', 'IN_PROGRESS', 'SUCCESS')
            ORDER BY
                CASE a.state
                    WHEN 'CONFIRMED' THEN 1
                    WHEN 'IN_PROGRESS' THEN 2
                    WHEN 'SUCCESS' THEN 3
                    ELSE 4
                END ASC,
                a.rental_date ASC,
                a.id ASC
            LIMIT :limit
        """, nativeQuery = true
    )
    List<Appointment> findMyAppointmentFirstPageAsRequester(
            @Param("requesterId") Long requesterId,
            @Param("start") LocalDateTime start,
            @Param("limit") int limit
    );

    /// 소유자 ID, 검색 시작 날짜, 게시글 타입으로 (확정/대여중/완료) 상태인 약속 리스트 조회

    // 커서 기반 페이지네이션 (cursor: appointmentState, rentalDate, id)
    // AppointmentState 기준 : CONFIRMED > IN_PROGRESS > SUCCESS
    // 동일한 AppointmentState 내에서는 rentalDate 오름차순
    // 그것까지 동일하면 ID 오름차순
    @Query(
            value = """
            SELECT a.* FROM appointment a JOIN item i ON a.item = i.id
            WHERE a.owner = :ownerId
                AND a.rental_date >= :start
                AND a.state IN ('CONFIRMED', 'IN_PROGRESS', 'SUCCESS')
                AND (
                    CASE a.state
                        WHEN 'CONFIRMED' THEN 1
                        WHEN 'IN_PROGRESS' THEN 2
                        WHEN 'SUCCESS' THEN 3
                        ELSE 4
                    END > CASE :cursorState
                        WHEN 'CONFIRMED' THEN 1
                        WHEN 'IN_PROGRESS' THEN 2
                        WHEN 'SUCCESS' THEN 3
                        ELSE 4
                    END
                    OR (
                        CASE a.state
                            WHEN 'CONFIRMED' THEN 1
                            WHEN 'IN_PROGRESS' THEN 2
                            WHEN 'SUCCESS' THEN 3
                            ELSE 4
                        END = CASE :cursorState
                            WHEN 'CONFIRMED' THEN 1
                            WHEN 'IN_PROGRESS' THEN 2
                            WHEN 'SUCCESS' THEN 3
                            ELSE 4
                        END
                        AND (
                            a.rental_date > :cursorDate
                            OR (a.rental_date = :cursorDate AND a.id >= :cursorId)
                        )
                    )
                )
            ORDER BY
                CASE a.state
                    WHEN 'CONFIRMED' THEN 1
                    WHEN 'IN_PROGRESS' THEN 2
                    WHEN 'SUCCESS' THEN 3
                    ELSE 4
                END ASC,
                a.rental_date ASC,
                a.id ASC
            LIMIT :limit
        """, nativeQuery = true
    )
    List<Appointment> findMyAppointmentPageAsOwner(
            @Param("ownerId") Long ownerId,
            @Param("start") LocalDateTime start,
            @Param("cursorState") String cursorState,
            @Param("cursorDate") LocalDateTime cursorDate,
            @Param("cursorId") Long cursorId,
            @Param("limit") int limit
    );

    // 첫 페이지 조회
    @Query(
            value = """
            SELECT a.* FROM appointment a JOIN item i ON a.item = i.id
            WHERE a.owner = :ownerId
                AND a.rental_date >= :start
                AND a.state IN ('CONFIRMED', 'IN_PROGRESS', 'SUCCESS')
            ORDER BY
                CASE a.state
                    WHEN 'CONFIRMED' THEN 1
                    WHEN 'IN_PROGRESS' THEN 2
                    WHEN 'SUCCESS' THEN 3
                    ELSE 4
                END ASC,
                a.rental_date ASC,
                a.id ASC
            LIMIT :limit
        """, nativeQuery = true
    )
    List<Appointment> findMyAppointmentFirstPageAsOwner(
            @Param("ownerId") Long ownerId,
            @Param("start") LocalDateTime start,
            @Param("limit") int limit
    );

    // 만료되어야 하는 약속 페이지 조회
    // 1. 레코드 수정 일시가 현재의 일주일 전보다 이전인 PENDING 약속
    // 2. 대여 일시가 현재보다 이전인 PENDING 약속
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.state = 'PENDING' AND (
            (a.updatedAt <= :nowMinusWeek) OR
            (a.rentalDate <= :now)
        )
    """)
    Page<Appointment> findShouldBeExpiredAppointments(
            @Param("now") LocalDateTime now,
            @Param("nowMinusWeek") LocalDateTime nowMinusWeek,
            Pageable pageable
    );

    // 반납 날짜가 특정 시점 이전이고 특정 상태인 약속 페이지 조회 (SUCCESS 배치용)
    Page<Appointment> findByStateAndReturnDateBefore(AppointmentState state, LocalDateTime localDateTime, Pageable pageable);

    // 약속 ID, 사용자 ID, 약속 상태로 약속 조회
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.id = :id AND (a.requester.id = :userId OR a.owner.id = :userId)
    """)
    Optional<Appointment> findByIdAndUserId(Long id, Long userId);

    // 성공 처리되어야 하는 약속 페이지 조회
    @Query("""
        SELECT a FROM Appointment a
        WHERE (a.rentalDate < :time AND a.state = 'CONFIRMED' AND a.type = 'SALE')
           OR (a.returnDate < :time AND a.state = 'IN_PROGRESS' AND a.type = 'RENTAL')
    """)
    Page<Appointment> findShouldBeSuccessAppointments(
            @Param("time") LocalDateTime time,
            Pageable pageable
    );
}
