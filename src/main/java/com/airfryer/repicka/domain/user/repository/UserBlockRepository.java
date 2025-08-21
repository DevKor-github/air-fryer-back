package com.airfryer.repicka.domain.user.repository;

import com.airfryer.repicka.domain.user.entity.user_block.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long>
{
    /// 차단자 ID, 피차단자 ID로 유저 차단 데이터 조회

    Optional<UserBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    /// 두 사용자 ID로 유저 차단 데이터 존재 여부 확인

    @Query("""
        SELECT CASE WHEN COUNT(ub) > 0 THEN true ELSE false END
        FROM UserBlock ub
        WHERE (ub.blocker.id = :user1Id AND ub.blocked.id = :user2Id)
           OR (ub.blocker.id = :user2Id AND ub.blocked.id = :user1Id)
    """)
    boolean existsByUserIds(@Param("user1Id") Long user1Id,
                            @Param("user2Id") Long user2Id);
}
