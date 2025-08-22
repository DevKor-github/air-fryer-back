package com.airfryer.repicka.domain.chat.repository;

import com.airfryer.repicka.domain.chat.entity.Chat;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends MongoRepository<Chat, ObjectId>
{
    /// 채팅방 ID로 채팅 리스트 조회

    // 커서 기반 페이지네이션 (cursor: 채팅 ID)
    // 최신순 정렬
    @Query(value = "{ 'chatRoomId': ?0, '_id': { $lte: ?1 }, 'createdAt': { $gte: ?2 } }", sort = "{ '_id': -1 }")
    List<Chat> findChatList(Long chatRoomId, ObjectId cursorId, LocalDateTime lastReEnterAt, Pageable pageable);

    // 첫 페이지 조회
    @Query(value = "{ 'chatRoomId': ?0, 'createdAt': { $gte: ?1 } }", sort = "{ '_id': -1 }")
    List<Chat> findFirstChatList(Long chatRoomId, LocalDateTime lastReEnterAt, Pageable pageable);

    /// 채팅방 ID로 가장 최근 채팅 조회

    Optional<Chat> findFirstByChatRoomIdOrderByIdDesc(Long chatRoomId);
}
