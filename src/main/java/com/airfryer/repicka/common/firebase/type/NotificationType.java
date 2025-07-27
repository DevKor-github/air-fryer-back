package com.airfryer.repicka.common.firebase.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    
    APPOINTMENT_REMINDER("약속 리마인더 알림", "'%s' PICK 약속 날짜가 다가오고 있습니다! 잊지 말고 준비해주세요."),
    APPOINTMENT_CONFIRMATION("약속 확정 알림", "요청하신 '%s' PICK이 확정되었습니다."),
    CHAT_MESSAGE("새로운 메시지", "'%s'님이 새로운 메시지를 보냈습니다.");
    
    private final String title;
    private final String bodyFormat;
    
    public String getFormattedBody(String name) {
        return String.format(this.bodyFormat, name);
    }
}   