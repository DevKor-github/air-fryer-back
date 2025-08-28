package com.airfryer.repicka.domain.notification.repository;

import com.airfryer.repicka.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>
{
    /// 사용자 ID로 알림 리스트를 생성 날짜 내림차순으로 조회

    // 첫 페이지 조회 (최신순)
    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.id = :userId
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<Notification> findFirstPageByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // 커서 기반 다음 페이지 조회
    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.id = :userId AND (
            n.createdAt < :cursorCreatedAt OR
            (n.createdAt = :cursorCreatedAt AND n.id <= :cursorId)
        )
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<Notification> findPageByUserId(
            @Param("userId") Long userId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
