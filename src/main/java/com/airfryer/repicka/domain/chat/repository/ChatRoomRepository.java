package com.airfryer.repicka.domain.chat.repository;

import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>
{
    /// 제품 ID, 소유자 ID, 요청자 ID로 채팅방 조회

    Optional<ChatRoom> findByItemIdAndOwnerIdAndRequesterId(Long itemId, Long ownerId, Long requesterId);

    /// 두 사용자 ID로 채팅방 조회

    @Query("""
        SELECT c FROM ChatRoom c
        WHERE (c.owner.id = :user1Id AND c.requester.id = :user2Id) OR (c.owner.id = :user2Id AND c.requester.id = :user1Id)
    """)
    List<ChatRoom> findByParticipantIds(@Param("user1Id") Long user1Id,
                                        @Param("user2Id") Long user2Id);
}
