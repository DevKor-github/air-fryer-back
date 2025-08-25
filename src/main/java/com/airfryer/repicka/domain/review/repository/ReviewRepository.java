package com.airfryer.repicka.domain.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.airfryer.repicka.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long>
{
    List<Review> findByReviewedId(Long reviewedId);

    List<Review> findByReviewerId(Long reviewerId);

    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.appointment.id = :appointmentId AND r.reviewer.id = :reviewerId")
    boolean existsByAppointmentIdAndReviewerId(Long appointmentId, Long reviewerId);

    Optional<Review> findByReviewedIdAndReviewerIdAndAppointmentId(Long reviewedId, Long reviewerId, Long appointmentId);
}
