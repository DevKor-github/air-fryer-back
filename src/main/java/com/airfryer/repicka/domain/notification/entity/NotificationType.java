package com.airfryer.repicka.domain.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    APPOINTMENT_PROPOSAL("약속 제시 알림", "'%s'님이 새로운 약속을 제시하였습니다."),
    APPOINTMENT_CANCEL("약속 취소 알림", "'%s'님이 약속을 취소하였습니다."),
    APPOINTMENT_REMIND("약속 리마인드 알림", "'%s' PICK 날짜가 다가오고 있습니다! 잊지 말고 준비해주세요."),
    APPOINTMENT_CONFIRM("약속 확정 알림", "요청하신 '%s' PICK이 확정되었습니다."),
    APPINTMENT_EXPIRE("약속 만료 알림", "'%s' PICK 날짜가 만료되었습니다."),
    APPOINTMENT_REJECT("약속 거절 알림", "'%s'님이 약속을 거절하였습니다."),
    CHAT_MESSAGE("새로운 메시지", "'%s'님이 새로운 메시지를 보냈습니다.");
    
    private final String title;
    private final String bodyFormat;
    
    public String getFormattedBody(String name) {
        return String.format(this.bodyFormat, name);
    }
}
