package com.airfryer.repicka.domain.chat.repository;

import com.airfryer.repicka.domain.chat.entity.ParticipateChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipateChatRoomRepository extends JpaRepository<ParticipateChatRoom, Long>
{
    /// 채팅방 ID, 사용자 ID로 채팅방 참여 정보 조회
    Optional<ParticipateChatRoom> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}
