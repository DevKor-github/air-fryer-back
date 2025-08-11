package com.airfryer.repicka.domain.chat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateChatRoomReq
{
    @NotNull
    private Long itemId;
}
