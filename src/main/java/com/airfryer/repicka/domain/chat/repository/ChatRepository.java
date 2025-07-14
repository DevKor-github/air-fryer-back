package com.airfryer.repicka.domain.chat.repository;

import com.airfryer.repicka.domain.chat.entity.Chat;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ChatRepository extends ReactiveMongoRepository<Chat, ObjectId> {
}
