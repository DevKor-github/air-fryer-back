package com.airfryer.repicka.common.redis.dto;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class KeyExpiredEvent extends ApplicationEvent {
    
    private final String expiredKey;
    
    public KeyExpiredEvent(Object source, String expiredKey) {
        super(source);
        this.expiredKey = expiredKey;
    }
}