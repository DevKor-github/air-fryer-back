package com.airfryer.repicka.domain.chat.entity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Chat
{
    @Id
    private ObjectId id;

    // 채팅방 ID
    @NotNull
    private Long chatRoomId;

    // 사용자 ID
    private Long userId;

    // 내용
    @NotBlank
    private String content;
}
