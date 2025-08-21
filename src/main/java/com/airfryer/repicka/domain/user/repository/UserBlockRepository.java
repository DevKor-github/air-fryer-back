package com.airfryer.repicka.domain.user.repository;

import com.airfryer.repicka.domain.user.entity.user_block.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long>
{
    // 차단자 ID, 피차단자 ID로 유저 차단 데이터 조회
    Optional<UserBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
}
