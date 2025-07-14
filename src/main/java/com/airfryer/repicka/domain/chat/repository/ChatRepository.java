package com.airfryer.repicka.domain.chat.repository;

import com.airfryer.repicka.domain.chat.entity.Chat;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepository extends ReactiveMongoRepository<Chat, ObjectId>
{
    /// 채팅방 ID로 채팅 리스트 조회

    // 커서 기반 페이지네이션 (cursor: createdAt, id)
    // createdAt 내림차순
    // 동일한 createdAt 내에서는 ID 오름차순
    @Query(
            value = """
                {
                  "chatRoomId": ?0,
                  "$or": [
                    { "createdAt": { "$lt": ?1 } },
                    { "createdAt": ?1, "_id": { "$gt": ?2 } }
                  ]
                }
            """,
            sort = "{ 'createdAt' : -1, '_id' : 1 }"
    )
    Flux<Chat> findPageByChatRoomId(
            Long chatRoomId,
            LocalDateTime chatCursorCreatedAt,
            ObjectId cursorId,
            Pageable pageable
    );

    // 첫 페이지 조회
    @Query(
            value = """
                {
                  "chatRoomId": ?0
                }
            """,
            sort = "{ 'createdAt' : -1, '_id' : 1 }"
    )
    Flux<Chat> findFirstPageByChatRoomId(
            Long chatRoomId,
            Pageable pageable
    );
}
