package com.airfryer.repicka.domain.user.repository;

import com.airfryer.repicka.domain.user.entity.user_report.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long>
{
    // 신고자 ID, 피신고자 ID, 제품 ID로 유저 신고 데이터 조회
    Optional<UserReport> findByReporterIdAndReportedIdAndItemId(Long reporterId, Long reportedId, Long itemId);
}
