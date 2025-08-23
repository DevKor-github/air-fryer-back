package com.airfryer.repicka.domain.chat.repository;

import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipateChatRoomRepository extends JpaRepository<ParticipateChatRoom, Long>
{
    /// 채팅방 ID, 사용자 ID로 채팅방 참여 정보 조회

    Optional<ParticipateChatRoom> findByChatRoomIdAndParticipantId(Long chatRoomId, Long participantId);

    /// 사용자 ID로 나가지 않은 채팅방 참여 정보 페이지 조회

    // 첫 페이지 조회
    @Query("""
        SELECT pc FROM ParticipateChatRoom pc
        WHERE pc.participant.id = :userId AND pc.hasLeftRoom = false
        ORDER BY pc.chatRoom.lastChatAt DESC, pc.chatRoom.id DESC
    """)
    List<ParticipateChatRoom> findFirstPageByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // 처음 이후 페이지 조회
    @Query("""
        SELECT pc FROM ParticipateChatRoom pc
        WHERE pc.participant.id = :userId AND pc.hasLeftRoom = false
          AND (
            pc.chatRoom.lastChatAt < :cursorLastChatAt
            OR (pc.chatRoom.lastChatAt = :cursorLastChatAt AND pc.chatRoom.id <= :cursorId)
          )
        ORDER BY pc.chatRoom.lastChatAt DESC, pc.chatRoom.id DESC
    """)
    List<ParticipateChatRoom> findPageByUserId(
            @Param("userId") Long userId,
            @Param("cursorLastChatAt") LocalDateTime cursorLastChatAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /// 사용자 ID, 제품 ID로 나가지 않은 채팅방 참여 정보 페이지 조회

    // 첫 페이지 조회
    @Query("""
        SELECT pc FROM ParticipateChatRoom pc
        WHERE pc.chatRoom.owner.id = :ownerId AND pc.chatRoom.item.id = :itemId AND pc.hasLeftRoom = false
        ORDER BY pc.chatRoom.lastChatAt DESC, pc.chatRoom.id DESC
    """)
    List<ParticipateChatRoom> findFirstPageByOwnerIdAndItemId(
            @Param("ownerId") Long ownerId,
            @Param("itemId") Long itemId,
            Pageable pageable
    );

    // 처음 이후 페이지 조회
    @Query("""
        SELECT pc FROM ParticipateChatRoom pc
        WHERE pc.chatRoom.owner.id = :ownerId AND pc.chatRoom.item.id = :itemId AND pc.hasLeftRoom = false
          AND (
            pc.chatRoom.lastChatAt < :cursorLastChatAt
            OR (pc.chatRoom.lastChatAt = :cursorLastChatAt AND pc.chatRoom.id <= :cursorId)
          )
        ORDER BY pc.chatRoom.lastChatAt DESC, pc.chatRoom.id DESC
    """)
    List<ParticipateChatRoom> findPageByOwnerIdAndItemId(
            @Param("ownerId") Long ownerId,
            @Param("itemId") Long itemId,
            @Param("cursorLastChatAt") LocalDateTime cursorLastChatAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
