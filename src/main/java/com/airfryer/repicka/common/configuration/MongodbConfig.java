package com.airfryer.repicka.common.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Configuration
@EnableMongoAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@RequiredArgsConstructor
public class MongodbConfig implements InitializingBean
{
    private final MappingMongoConverter mappingMongoConverter;

    @Override
    public void afterPropertiesSet() {
        mappingMongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    }

    // Auditing 서울 시간대 적용
    @Bean(name = "auditingDateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now(ZoneId.of("Asia/Seoul")));
    }
}
