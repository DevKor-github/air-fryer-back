package com.airfryer.repicka.domain.notification.repository;

import com.airfryer.repicka.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>
{
    // 사용자 ID로 알림 리스트를 생성 날짜 내림차순으로 조회
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}
