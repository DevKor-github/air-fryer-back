package com.airfryer.repicka.domain.notification.repository;

import com.airfryer.repicka.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("SELECT n FROM Notification n " +
           "LEFT JOIN FETCH n.item i " +
           "LEFT JOIN FETCH n.appointment a " +
           "WHERE n.user.id = :userId " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
