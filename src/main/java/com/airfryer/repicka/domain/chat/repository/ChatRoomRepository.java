package com.airfryer.repicka.domain.chat.repository;

import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>
{
    /// 사용자 ID로 채팅방 페이지 조회

    // 첫 페이지 조회
    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE cr.requester.id = :userId OR cr.owner.id = :userId
        ORDER BY cr.lastChatAt DESC, cr.id DESC
    """)
    List<ChatRoom> findFirstPageByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // 처음 이후 페이지 조회
    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE (cr.requester.id = :userId OR cr.owner.id = :userId)
          AND (
            cr.lastChatAt < :cursorLastChatAt
            OR (cr.lastChatAt = :cursorLastChatAt AND cr.id <= :cursorId)
          )
        ORDER BY cr.lastChatAt DESC, cr.id DESC
    """)
    List<ChatRoom> findPageByUserId(
            @Param("userId") Long userId,
            @Param("cursorLastChatAt") LocalDateTime cursorLastChatAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /// 제품 ID로 채팅방 페이지 조회

    // 첫 페이지 조회
    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE cr.item.id = :itemId
        ORDER BY cr.lastChatAt DESC, cr.id DESC
    """)
    List<ChatRoom> findFirstPageByItemId(
            @Param("itemId") Long itemId,
            Pageable pageable
    );

    // 처음 이후 페이지 조회
    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE (cr.item.id = :itemId)
          AND (
            cr.lastChatAt < :cursorLastChatAt
            OR (cr.lastChatAt = :cursorLastChatAt AND cr.id <= :cursorId)
          )
        ORDER BY cr.lastChatAt DESC, cr.id DESC
    """)
    List<ChatRoom> findPageByItemId(
            @Param("itemId") Long itemId,
            @Param("cursorLastChatAt") LocalDateTime cursorLastChatAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /// 제품 ID, 소유자 ID, 요청자 ID로 채팅방 조회

    Optional<ChatRoom> findByItemIdAndOwnerIdAndRequesterId(Long itemId, Long ownerId, Long requesterId);

    /// 두 사용자 ID로 채팅방 조회

    @Query("""
        SELECT c FROM ChatRoom c
        WHERE (c.owner.id = :user1Id AND c.requester.id = :user2Id) OR (c.owner.id = :user2Id AND c.requester.id = :user1Id)
    """)
    List<ChatRoom> findByParticipants(@Param("user1Id") Long user1Id,
                                      @Param("user2Id") Long user2Id);
}
