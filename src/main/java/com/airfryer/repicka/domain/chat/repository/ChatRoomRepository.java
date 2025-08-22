package com.airfryer.repicka.domain.chat.repository;

import com.airfryer.repicka.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>
{
    /// 제품 ID, 소유자 ID, 요청자 ID로 채팅방 조회

    Optional<ChatRoom> findByItemIdAndOwnerIdAndRequesterId(Long itemId, Long userId, Long requesterId);
}
