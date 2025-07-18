package com.airfryer.repicka.domain.chat.repository;

import com.airfryer.repicka.domain.chat.entity.Chat;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ChatRepository extends ReactiveMongoRepository<Chat, ObjectId>
{
    /// 채팅방 ID로 채팅 리스트 조회

    // 커서 기반 페이지네이션 (cursor: id)
    // 최신순 정렬
    Flux<Chat> findByChatRoomIdAndIdLessThanOrderByIdDesc(Long chatRoomId, ObjectId id, Pageable pageable);

    // 첫 페이지 조회
    Flux<Chat> findByChatRoomIdOrderByIdDesc(Long chatRoomId, Pageable pageable);
}
