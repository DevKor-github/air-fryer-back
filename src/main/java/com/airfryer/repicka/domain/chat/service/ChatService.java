package com.airfryer.repicka.domain.chat.service;

import com.airfryer.repicka.domain.chat.dto.EnterChatRoomRes;
import com.airfryer.repicka.domain.chat.repository.ChatRoomRepository;
import com.airfryer.repicka.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService
{
    private final ChatRoomRepository chatRoomRepository;

    public EnterChatRoomRes enterChatRoom(User user, Long chatRoomId)
    {

    }
}
