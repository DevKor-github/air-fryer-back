package com.airfryer.repicka.common.batch.configuration;

import com.airfryer.repicka.common.firebase.dto.FCMNotificationReq;
import com.airfryer.repicka.common.firebase.service.FCMService;
import com.airfryer.repicka.domain.appointment.entity.Appointment;
import com.airfryer.repicka.domain.appointment.entity.UpdateInProgressAppointment;
import com.airfryer.repicka.domain.appointment.repository.AppointmentRepository;
import com.airfryer.repicka.domain.appointment.repository.UpdateInProgressAppointmentRepository;
import com.airfryer.repicka.domain.notification.entity.NotificationType;
import com.airfryer.repicka.domain.notification.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// 약속 State 변경 관련 Spring Batch 설정

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class AppointmentBatchConfig
{
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final AppointmentRepository appointmentRepository;
    private final UpdateInProgressAppointmentRepository updateInProgressAppointmentRepository;

    private final FCMService fcmService;
    private final NotificationService notificationService;

    // 약속 만료 Job
    @Bean
    public Job expireAppointmentJob(Step expireAppointmentStep)
    {
        return new JobBuilder("expireAppointmentJob", jobRepository)
            .start(expireAppointmentStep)
            .build();
    }

    // 약속 성공 처리 Job
    @Bean
    public Job successAppointmentJob(Step successAppointmentStep)
    {
        return new JobBuilder("successAppointmentJob", jobRepository)
            .start(successAppointmentStep)
            .build();
    }

    // 약속 만료 Step
    @Bean
    public Step expireAppointmentStep(ItemReader<Appointment> expireAppointmentReader,
                                      ItemWriter<Appointment> expireAppointmentWriter)
    {
        return new StepBuilder("expireAppointmentStep", jobRepository)
            .<Appointment, Appointment>chunk(100, transactionManager)
            .reader(expireAppointmentReader)
            .writer(expireAppointmentWriter)
            .build();
    }

    // 약속 성공 처리 Step
    @Bean
    public Step successAppointmentStep(ItemReader<Appointment> successAppointmentReader,
                                       ItemWriter<Appointment> successAppointmentWriter)
    {
        return new StepBuilder("successAppointmentStep", jobRepository)
            .<Appointment, Appointment>chunk(100, transactionManager)
            .reader(successAppointmentReader)
            .writer(successAppointmentWriter)
            .build();
    }

    // 약속 만료 reader
    @Bean
    @StepScope
    public ItemReader<Appointment> expireAppointmentReader(@Value("#{jobParameters['now']}") String now)
    {
        // 1. 레코드 수정 일시가 현재의 일주일 전보다 이전인 PENDING 약속
        // 2. 대여 일시가 현재보다 이전인 PENDING 약속
        return new RepositoryItemReaderBuilder<Appointment>()
            .repository(appointmentRepository)
            .methodName("findShouldBeExpiredAppointments")
            .arguments(List.of(LocalDateTime.parse(now), LocalDateTime.parse(now).minusWeeks(1)))
            .pageSize(100)
            .sorts(Map.of("updatedAt", Sort.Direction.ASC))
            .name("expireAppointmentReader")
            .build();
    }

    // 약속 성공 처리 reader
    @Bean
    @StepScope
    public ItemReader<Appointment> successAppointmentReader(@Value("#{jobParameters['now']}") String now)
    {
        // returnDate가 현재 시점 이전이고 IN_PROGRESS 상태인 Appointment 조회
        return new RepositoryItemReaderBuilder<Appointment>()
            .repository(appointmentRepository)
            .methodName("findShouldBeSuccessAppointments")
            .arguments(List.of(LocalDateTime.parse(now)))
            .pageSize(100)
            .sorts(Map.of("id", Sort.Direction.ASC))
            .name("successAppointmentReader")
            .build();
    }

    // PENDING 약속 만료 writer
    @Bean
    public ItemWriter<Appointment> expireAppointmentWriter()
    {
        return appointments -> {

            /// 1. 약속 상태를 EXPIRED로 변경
            /// 2. 푸시알림 전송 및 알림 내역 저장
            /// 3. 연관된 UpdateInProgressAppointment 데이터 삭제

            for(Appointment appointment : appointments)
            {
                // 약속 상태를 EXPIRED로 변경
                appointment.expire();

                // 약속 만료 푸시알림 전송
                FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_EXPIRE, appointment.getId().toString(), appointment.getItem().getTitle());
                fcmService.sendNotification(appointment.getCreator().getFcmToken(), notificationReq);

                // 약속 만료 알림 내역 저장
                notificationService.saveNotification(appointment.getCreator(), NotificationType.APPOINTMENT_EXPIRE, appointment);
            }

            // 약속 ID 리스트
            List<Long> appointmentIdList = appointments.getItems().stream()
                .map(Appointment::getId)
                .toList();

            // 연관된 모든 UpdateInProgressAppointment 데이터 삭제
            List<UpdateInProgressAppointment> deleteList = updateInProgressAppointmentRepository.findByAppointmentIdIn(appointmentIdList);
            updateInProgressAppointmentRepository.deleteAll(deleteList);

        };
    }

    // IN_PROGRESS 약속 성공 처리 writer
    @Bean
    public ItemWriter<Appointment> successAppointmentWriter()
    {
        return appointments -> {

            /// 1. 약속 상태를 SUCCESS로 변경
            /// 2. 푸시알림 전송 및 알림 내역 저장
            /// 3. 연관된 UpdateInProgressAppointment 데이터 삭제

            for(Appointment appointment : appointments)
            {
                // 약속 상태를 SUCCESS로 변경
                appointment.success();

                // 약속 완료 푸시알림 전송
                FCMNotificationReq notificationReq = FCMNotificationReq.of(NotificationType.APPOINTMENT_SUCCESS, appointment.getId().toString(), appointment.getItem().getTitle());
                fcmService.sendNotification(appointment.getCreator().getFcmToken(), notificationReq);

                // 약속 완료 알림 내역 저장
                notificationService.saveNotification(appointment.getCreator(), NotificationType.APPOINTMENT_SUCCESS, appointment);
            }

            // 약속 ID 리스트
            List<Long> appointmentIdList = appointments.getItems().stream()
                .map(Appointment::getId)
                .toList();

            // 연관된 모든 UpdateInProgressAppointment 데이터 삭제
            List<UpdateInProgressAppointment> deleteList = updateInProgressAppointmentRepository.findByAppointmentIdIn(appointmentIdList);
            updateInProgressAppointmentRepository.deleteAll(deleteList);

        };
    }
}
