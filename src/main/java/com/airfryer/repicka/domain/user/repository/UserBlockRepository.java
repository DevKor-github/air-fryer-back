package com.airfryer.repicka.domain.user.repository;

import com.airfryer.repicka.domain.user.entity.user_report.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBlockRepository extends JpaRepository<UserReport, Long> {
}
